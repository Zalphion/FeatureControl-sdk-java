package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.FeatureBundle
import com.zalphion.featurecontrol.client.BundleStillCurrent
import com.zalphion.featurecontrol.client.FeatureControlClient
import com.zalphion.featurecontrol.client.TaggedSdkBundle
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

fun FeatureFlags.Companion.http(
    client: FeatureControlClient,
    sdkKey: String
) = object: FeatureFlags {

    val latest = AtomicReference<TaggedSdkBundle>()

    override fun getBundle(): Result4k<FeatureBundle, String> {
        val response = client.getBundle(sdkKey, latest.get()?.eTag).onFailure { return it }
        when (response) {
            is TaggedSdkBundle -> latest.set(response)
            is BundleStillCurrent -> {}
        }
        return latest.get().bundle.asSuccess()
    }

    override val readyFuture: CompletableFuture<FeatureFlags> = CompletableFuture.completedFuture(this)

    override fun close() {}
}