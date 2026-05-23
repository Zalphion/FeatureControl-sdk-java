package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.FeatureBundle
import dev.forkhandles.result4k.Result4k
import java.util.concurrent.CompletableFuture

interface FeatureFlags: AutoCloseable {
    fun getBundle(): Result4k<FeatureBundle, String>

    val readyFuture: CompletableFuture<FeatureFlags>

    companion object
}