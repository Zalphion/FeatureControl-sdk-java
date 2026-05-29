package com.zalphion.featurecontrol.lib.http.cache;

import com.zalphion.featurecontrol.lib.http.HttpFunction;
import com.zalphion.featurecontrol.lib.http.HttpMethod;
import com.zalphion.featurecontrol.lib.http.HttpRequest;
import com.zalphion.featurecontrol.lib.http.HttpResponse;
import lombok.*;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Caching layer that respects the server's cache control and eTag headers;
 */
@RequiredArgsConstructor
public class CacheAwareHttpFunction implements HttpFunction {
    private static final String
            IF_NONE_MATCH = "If-None-Match",
            AUTHORIZATION = "Authorization";

    private static final int OK = 200, NOT_MODIFIED = 304;
    private static final Duration DEFAULT_MAX_AGE = Duration.ofMinutes(1);

    private final @lombok.NonNull HttpFunction delegate;
    private final @lombok.NonNull Supplier<Instant> clock;

    public CacheAwareHttpFunction(HttpFunction delegate) {
        this(delegate, Instant::now);
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public @NonNull @lombok.NonNull HttpResponse exchange(@NonNull @lombok.NonNull HttpRequest request) {
        // only cache GET requests
        if (request.getMethod() != HttpMethod.GET) {
            return delegate.exchange(request);
        }

        val cached = cache.computeIfAbsent(cacheKey(request), key -> new CacheEntry(clock.get(), DEFAULT_MAX_AGE));

        // optimistic cache lookup
        {
            if (cached.getResponse() != null && cached.isNotExpired(clock.get())) {
                return cached.getResponse();
            }
        }

        synchronized (cached) {
            // secondary cache lookup after blocking on the lock
            if (cached.getResponse() != null && cached.isNotExpired(clock.get())) {
                return cached.getResponse();
            }

            // Make request (with If-None-Match if available)
            final HttpResponse response;
            if (cached.getResponse() != null && cached.getETag() != null) {
                response = delegate.exchange(request.withHeader(IF_NONE_MATCH, cached.getETag()));
            } else {
                response = delegate.exchange(request);
            }

            // Cache OK responses and extend the duration on a NOT_MODIFIED
            if (response.getStatusCode() != OK && response.getStatusCode() != NOT_MODIFIED) return response;

            // TODO only cache when cache-control header is present

            // update cache data
            cached.setUpdatedAt(clock.get());
            if (response.getStatusCode() == OK) cached.setResponse(response);
            CacheControl.from(response).ifPresent(cacheControl -> {
                cached.setMaxAge(cacheControl.getMaxAge());
                Optional.ofNullable(cacheControl.getETag()).ifPresent(cached::setETag);
            });

            // prune cache
            val now = clock.get();
            for (val entry : cache.entrySet()) {
                if (entry.getValue().shouldPurge(now)) {
                    synchronized (entry.getValue()) {
                        if (entry.getValue().shouldPurge(now)) {
                            cache.remove(entry.getKey());
                        }
                    }
                }
            }

            return cached.getResponse();
        }
    }

    private static @NonNull String cacheKey(@NonNull @lombok.NonNull HttpRequest request) {
        try {
            val digest = MessageDigest.getInstance("SHA-256");
            digest.update(("method=" + request.getMethod()).getBytes(StandardCharsets.UTF_8));
            digest.update((",url=" + request.getUrl()).getBytes(StandardCharsets.UTF_8));
            request.getHeaderValue(AUTHORIZATION).ifPresent(authorization ->
                    digest.update((",authorization=" + authorization).getBytes(StandardCharsets.UTF_8))
            );

            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest());

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to create SHA-256 digest", e);
        }
    }

    @RequiredArgsConstructor
    private static class CacheEntry {
        private static final int PURGE_THRESHOLD_MULTIPLIER = 2;

        private @Getter @Setter @NonNull @lombok.NonNull Instant updatedAt;
        private @Getter @Setter @NonNull @lombok.NonNull Duration maxAge;

        private @Setter @Getter HttpResponse response;
        private @Setter @Getter String eTag;

        public boolean isNotExpired(Instant now) {
            return updatedAt.plus(maxAge).isAfter(now);
        }

        public boolean shouldPurge(Instant now) {
            return updatedAt.plus(maxAge.multipliedBy(PURGE_THRESHOLD_MULTIPLIER)).isBefore(now);
        }
    }
}
