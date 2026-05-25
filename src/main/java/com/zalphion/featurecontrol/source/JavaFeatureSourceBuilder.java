package com.zalphion.featurecontrol.source;

import com.zalphion.featurecontrol.Features;
import com.zalphion.featurecontrol.client.FeatureControl;
import dev.forkhandles.result4k.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
                HttpFeatureSourceKt.toFeatureSource(
                        Objects.requireNonNull(client, "client is required"),
                        Objects.requireNonNull(sdkKey, "sdkKey is required")
                )
        );
    }

    public static @NotNull JavaFeatureSource memory(
            @NotNull Features features
    ) {
        Objects.requireNonNull(features, "features is required");
        return new JavaFeatureSource(
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

    /**
     * Fetch features in the background to not block execution.
     * Prefer this method over blocking() unless the application is short-lived.
     */
    public @NotNull JavaCloseableFeatureSource preFetching(
            @Nullable Duration refreshInterval,
            @Nullable Duration retryInterval,
            @Nullable ScheduledExecutorService scheduler
    ) {
        return new JavaCloseableFeatureSource(
                PreFetchingFeatureSourceKt.preFetching(
                        source,
                        refreshInterval == null ? Duration.ofMinutes(1) : refreshInterval,
                        retryInterval == null ? Duration.ofSeconds(1) : retryInterval,
                        scheduler == null ? Executors.newSingleThreadScheduledExecutor() : scheduler
                )
        );
    }

    /**
     * If cached features are expired, the thread will block until they are retrieved.
     * Use this only for short-lived applications.
     */
    public @NotNull JavaFeatureSource blocking() {
        return new JavaFeatureSource(source);
    }
}