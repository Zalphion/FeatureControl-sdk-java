package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.Features
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import java.util.concurrent.CompletableFuture

interface FeatureSource: AutoCloseable {
    fun get(): Result4k<Features, String>
    fun safeGet() = try { get() } catch (e: Exception) { (e.message ?: "Unknown error").asFailure() }

    val readyFuture: CompletableFuture<FeatureSource>

    companion object
}