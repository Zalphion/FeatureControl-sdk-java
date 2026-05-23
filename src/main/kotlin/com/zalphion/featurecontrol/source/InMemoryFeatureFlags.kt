package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.FeatureBundle
import dev.forkhandles.result4k.asSuccess
import java.util.concurrent.CompletableFuture

fun FeatureFlags.Companion.memory(bundle: FeatureBundle) = memory { bundle }

/**
 * Useful for testing where you want to control the feature bundle dynamically.
 */
fun FeatureFlags.Companion.memory(bundleFn: () -> FeatureBundle) = object: FeatureFlags {
    override fun getBundle() = bundleFn().asSuccess()
    override val readyFuture: CompletableFuture<FeatureFlags> = CompletableFuture.completedFuture(this)
    override fun close() {}
}