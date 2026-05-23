package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.FeatureBundle
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.recover
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.util.concurrent.CompletableFuture

/**
 * Useful for testing.  Read the bundle from a JSON file on the classpath.
 * Defaults to the Thread Context ClassLoader to ensure resources in the caller's module are visible.
 */
@OptIn(ExperimentalSerializationApi::class)
fun FeatureFlags.Companion.classpath(
    absolutePath: String,
    classLoader: ClassLoader = Thread.currentThread().contextClassLoader ?: FeatureFlags::class.java.classLoader
) = object: FeatureFlags {

    private val bundle = try {
        classLoader.getResourceAsStream(absolutePath)
            .asResultOr { "Could not read bundle from classpath: $absolutePath" }
            .map { Json.decodeFromStream(FeatureBundle.serializer(), it) }
    } catch (e: Exception) {
        "Failed to read bundle from classpath: $absolutePath: ${e.message}".asFailure()
    }

    override fun getBundle() = bundle
    override fun close() {}

    override val readyFuture: CompletableFuture<FeatureFlags> = bundle
        .map { CompletableFuture.completedFuture<FeatureFlags>(this) }
        .recover { CompletableFuture<FeatureFlags>().apply {
            completeExceptionally(IllegalStateException(it))
        } }
}