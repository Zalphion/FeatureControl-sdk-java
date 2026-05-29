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
import java.time.Clock;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private final @lombok.NonNull Clock clock;

    public CacheAwareHttpFunction(HttpFunction delegate) {
        this(delegate, Clock.systemUTC());
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    @Override
    public @NonNull @lombok.NonNull HttpResponse exchange(@NonNull @lombok.NonNull HttpRequest request) {
        // only cache GET requests
        if (request.getMethod() != HttpMethod.GET) {
            return delegate.exchange(request);
        }

        val cacheKey = cacheKey(request);

        // optimistic cache lookup
        {
            CacheEntry cached = cache.get(cacheKey);
            if (cached != null && clock.instant().isBefore(cached.getExpiresAt())) {
                return cached.getResponse();
            }
        }

        // TODO need to evict expired locks and cache entries?

        synchronized (locks.computeIfAbsent(cacheKey, k -> new Object())) {
            // secondary cache lookup after blocking on the lock
            val cached = cache.get(cacheKey);
            if (cached != null && clock.instant().isBefore(cached.getExpiresAt())) {
                return cached.getResponse();
            }

            // Make request (with If-None-Match if available)
            final HttpResponse response;
            if (cached != null && cached.getETag() != null) {
                response = delegate.exchange(request.withHeader(IF_NONE_MATCH, cached.getETag()));
            } else {
                response = delegate.exchange(request);
            }

            // Cache OK responses and extend the duration on a NOT_MODIFIED
            if (response.getStatusCode() != OK && response.getStatusCode() != NOT_MODIFIED) return response;
            if (response.getStatusCode() == NOT_MODIFIED && cached == null) return response;

            // create or update cache entry
            val cacheControl = CacheControl.from(response);
            val entry = new CacheEntry(
                    response.getStatusCode() == OK ? response : Objects.requireNonNull(cached).getResponse(),
                    clock.instant(),
                    cacheControl
                            .map(CacheControl::getMaxAge)
                            .orElseGet(() -> cached != null ? cached.getMaxAge() : DEFAULT_MAX_AGE),
                    cacheControl.isPresent() ? cacheControl.get().getETag() : cached != null ? cached.getETag() : null
            );
            cache.put(cacheKey, entry);

            // prune cache


            return entry.getResponse();
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
}
