package com.zalphion.featurecontrol.http;

import lombok.Data;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;

@Data
class CacheControl {
    private static final String
            E_TAG = "ETag",
            CACHE_CONTROL = "Cache-Control",
            CACHE_CONTROL_MAX_AGE = "max-age";

    private final @NonNull @lombok.NonNull Duration maxAge;
    private final @Nullable String eTag;

    static Optional<CacheControl> from(@NonNull @lombok.NonNull HttpResponse response) {
        val directives = response.getHeaderValue(CACHE_CONTROL)
                .map(Directives::parse)
                .orElse(new Directives(new HashMap<>()));

        val maxAgeSeconds = directives.getInt(CACHE_CONTROL_MAX_AGE);
        if (!maxAgeSeconds.isPresent()) return Optional.empty();
        val cacheControl = new CacheControl(
                Duration.ofSeconds(maxAgeSeconds.get()),
                response.getHeaderValue(E_TAG).orElse(null)
        );

        return Optional.of(cacheControl);
    }
}
