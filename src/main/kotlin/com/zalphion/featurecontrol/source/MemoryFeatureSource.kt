package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.Features
import com.zalphion.featurecontrol.GetProperty
import com.zalphion.featurecontrol.GetVariantOrNull
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import java.util.concurrent.CompletableFuture

fun FeatureSource.Companion.memory(
    flags: Map<String, GetVariantOrNull> = emptyMap(),
    properties: Map<String, GetProperty> = emptyMap(),
) = memory { Features(flags, properties).asSuccess() }
fun FeatureSource.Companion.memory(failure: String): FeatureSource = memory { failure.asFailure() }

fun FeatureSource.Companion.memory(flags: Features) = memory { flags.asSuccess() }

/**
 * Useful for testing where you want to control the feature bundle dynamically.
 */
fun FeatureSource.Companion.memory(bundleFn: () -> Result4k<Features, String>) = object: FeatureSource {
    override fun get() = bundleFn()
}