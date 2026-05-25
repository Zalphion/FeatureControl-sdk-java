package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.Features
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.peek
import dev.forkhandles.result4k.peekFailure
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Caches a FeatureBundle asynchronously with the help of a background thread.
 * The caller will never be blocked by a network call.
 */
fun FeatureSource.preFetching(
    refreshInternal: Duration = Duration.ofMinutes(1),
    retryInterval: Duration = Duration.ofSeconds(1),
    scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
) = object: CloseableFeatureSource {
    private val logger = LoggerFactory.getLogger(FeatureSource::class.java)
    private val inner = this@preFetching
    private val cache = AtomicReference<Features>()
    private val firstFetchFuture = CompletableFuture<FeatureSource>()

    private fun fetchNow() {
        if (scheduler.isShutdown) return

        try {
            inner.get().peek {
                cache.set(it)
                if (firstFetchFuture.complete(this)) {
                    logger.debug("Feature Bundle initialized successfully")
                } else {
                    logger.trace("Refreshed SDK Bundle")
                }
            }.peekFailure {
                logger.error("Failed to refresh SDK Bundle: $it")
                if (!firstFetchFuture.isDone && !scheduler.isShutdown) {
                    scheduler.schedule(::fetchNow, retryInterval.toMillis(), TimeUnit.MILLISECONDS)
                }
            }
        } catch (_: InterruptedException) {
            logger.debug("SDK Bundle refresh interrupted")
            Thread.currentThread().interrupt()
        } catch (e: Exception) {
            logger.error("Error refreshing SDK Bundle", e)
        }
    }

    init {
        scheduler.scheduleWithFixedDelay(::fetchNow, 0, refreshInternal.toMillis(), TimeUnit.MILLISECONDS)
    }

    override fun get() = cache.get().asResultOr { "A bundle has not yet been successfully fetched" }

    override fun close() = scheduler.shutdown()
}