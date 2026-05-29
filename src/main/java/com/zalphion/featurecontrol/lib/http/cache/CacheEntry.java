package com.zalphion.featurecontrol.lib.http.cache;

import com.zalphion.featurecontrol.lib.http.HttpResponse;
import lombok.*;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.time.Instant;

@Data
class CacheEntry {
    private final @NonNull @lombok.NonNull HttpResponse response;
    private final @NonNull @lombok.NonNull Instant updatedAt;
    private final @NonNull @lombok.NonNull Duration maxAge;
    private final String eTag;

    public @NonNull Instant getExpiresAt() {
        return updatedAt.plus(maxAge);
    }
}