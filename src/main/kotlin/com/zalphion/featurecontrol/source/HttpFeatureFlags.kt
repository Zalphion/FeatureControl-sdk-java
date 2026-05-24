package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.Features
import com.zalphion.featurecontrol.client.BundleStillCurrent
import com.zalphion.featurecontrol.client.FeatureControl
import com.zalphion.featurecontrol.client.TaggedSdkBundle
import com.zalphion.featurecontrol.toFeatures
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

fun FeatureSource.Companion.http(
    client: FeatureControl,
    sdkKey: String
) = object: FeatureSource {

    val latest = AtomicReference<TaggedSdkBundle>()

    override fun get(): Result4k<Features, String> {
        val response = client.getBundle(sdkKey, latest.get()?.eTag).onFailure { return it }
        when (response) {
            is TaggedSdkBundle -> latest.set(response)
            is BundleStillCurrent -> {}
        }
        return latest.get().bundle.toFeatures().asSuccess()
    }

    override val readyFuture: CompletableFuture<FeatureSource> = CompletableFuture.completedFuture(this)

    override fun close() {}
}