package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.FeatureBundle
import dev.forkhandles.result4k.Result4k
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

/**
 * Caches a FeatureBundle.  Fetches for an expired bundle will block.
 * Prefer the preFetchingFeatureBundleSource for its non-blocking operation.
 * Use this only if a background thread is undesirable, such as in short-lived applications.
 */
fun FeatureFlags.caching(
    ttl: Duration = Duration.ofMinutes(1),
    clock: Clock = Clock.systemUTC(),
) = object: FeatureFlags by this@caching {
    // Cache the entire result to avoid hammering the server when it returns an error
    private val cached = AtomicReference<Pair<Result4k<FeatureBundle, String>, Instant>>()

    private fun getIfCurrent(): Result4k<FeatureBundle, String>? = cached.get()
        ?.takeIf { (_, lastFetched) -> Duration.between(lastFetched, clock.instant()) < ttl }
        ?.first

    override fun getBundle(): Result4k<FeatureBundle, String> {
        getIfCurrent()?.let { return it }

        return synchronized(cached) {
            // Don't refresh again if it was refreshed while waiting for a lock
            getIfCurrent() ?: this@caching.getBundle().also {
                cached.set(it to clock.instant())
            }
        }
    }
}