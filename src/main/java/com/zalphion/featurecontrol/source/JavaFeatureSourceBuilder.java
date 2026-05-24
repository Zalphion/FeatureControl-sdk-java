package com.zalphion.featurecontrol.source;

import com.zalphion.featurecontrol.Features;
import com.zalphion.featurecontrol.client.FeatureControl;
import dev.forkhandles.result4k.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/**
 * A Java-friendly wrapper to compose a FeatureSource.
 */
public class JavaFeatureSourceBuilder {

    @NotNull private final FeatureSource source;

    private JavaFeatureSourceBuilder(@NotNull FeatureSource source) {
        this.source = source;
    }

    public static @NotNull JavaFeatureSourceBuilder http(
            @NotNull FeatureControl client,
            @NotNull String sdkKey
    ) {
        return new JavaFeatureSourceBuilder(
                HttpFeatureSourceKt.http(
                        FeatureSource.Companion,
                        Objects.requireNonNull(client, "client is required"),
                        Objects.requireNonNull(sdkKey, "sdkKey is required")
                )
        );
    }

    public static @NotNull JavaFeatureSourceBuilder memory(
            @NotNull Features features
    ) {
        Objects.requireNonNull(features, "features is required");
        return new JavaFeatureSourceBuilder(
                MemoryFeatureSourceKt.memory(
                        FeatureSource.Companion,
                        features
                )
        );
    }

    public static @NotNull JavaFeatureSourceBuilder memory(
            @NotNull Supplier<Result<Features, String>> featureSupplier
    ) {
        Objects.requireNonNull(featureSupplier, "featureSupplier is required");
        return new JavaFeatureSourceBuilder(
                MemoryFeatureSourceKt.memory(
                        FeatureSource.Companion,
                        featureSupplier::get
                )
        );
    }

    public @NotNull JavaFeatureSource preFetching(
            @Nullable Duration refreshInterval,
            @Nullable Duration retryInterval,
            @Nullable ScheduledExecutorService scheduler
    ) {
        return new JavaFeatureSource(
                PreFetchingFeatureSourceKt.preFetching(
                        source,
                        refreshInterval == null ? Duration.ofMinutes(1) : refreshInterval,
                        retryInterval == null ? Duration.ofSeconds(1) : retryInterval,
                        scheduler == null ? Executors.newSingleThreadScheduledExecutor() : scheduler
                )
        );
    }

    public @NotNull JavaFeatureSource caching(
            @Nullable Duration ttl,
            @Nullable Clock clock
    ) {
        return new JavaFeatureSource(
                CachingFeatureSourceKt.caching(
                        source,
                        ttl == null ? Duration.ofMinutes(1) : ttl,
                        clock == null ? Clock.systemUTC() : clock
                )
        );
    }

    public @NotNull JavaFeatureSource uncached() {
        return new JavaFeatureSource(source);
    }
}