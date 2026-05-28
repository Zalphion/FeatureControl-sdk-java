package com.zalphion.featurecontrol.source;

import com.zalphion.featurecontrol.bundle.FeatureBundle;
import com.zalphion.featurecontrol.lib.result.Result;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class PreFetchingFeatureSource extends FeatureSource implements AutoCloseable {
    private final @lombok.NonNull FeatureSource inner;
    private final @lombok.NonNull Duration retryInterval;
    private final @lombok.NonNull ScheduledExecutorService scheduler;

    // FIXME don't include in lombok
    private final AtomicReference<FeatureBundle> cache = new AtomicReference<>();

    public PreFetchingFeatureSource(@NonNull @lombok.NonNull FeatureSource inner) {
        this(inner, Duration.ofSeconds(10), Duration.ofSeconds(1), Executors.newSingleThreadScheduledExecutor());
    }

    public PreFetchingFeatureSource(
            @NonNull @lombok.NonNull FeatureSource inner,
            @NonNull @lombok.NonNull Duration refreshInterval,
            @NonNull @lombok.NonNull Duration retryInterval,
            @NonNull @lombok.NonNull ScheduledExecutorService scheduler
    ) {
        this.inner = inner;
        this.retryInterval = retryInterval;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleWithFixedDelay(this::fetchNow, 0, refreshInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected @NonNull @lombok.NonNull Result<FeatureBundle> getInternal() {
        return Result.successOr(cache.get(), () -> "A bundle has not yet been successfully fetched");
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }

    private void fetchNow() {
        if (scheduler.isShutdown()) return;

        try {
            inner.get().peek(value -> {
                val previous = cache.getAndSet(value);
                if (previous == null) {
                    log.debug("Ready");
                } else {
                    log.trace("Refreshed");
                }
            }).peekFailure(message -> {
                    log.warn(message);
                    if (cache.get() == null && !scheduler.isShutdown()) {
                        scheduler.schedule(this::fetchNow, retryInterval.toMillis(), TimeUnit.MILLISECONDS);
                    }
            });
        } catch (RuntimeException e) {
            log.warn("Error refreshing SDK Bundle", e);
        }
    }
}