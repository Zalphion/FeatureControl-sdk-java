package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.FeatureBundle
import com.zalphion.featurecontrol.toFeatures
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
fun FeatureSource.Companion.classpath(
    absolutePath: String,
    classLoader: ClassLoader = Thread.currentThread().contextClassLoader ?: FeatureSource::class.java.classLoader
) = object: FeatureSource {

    private val bundle = try {
        classLoader.getResourceAsStream(absolutePath)
            .asResultOr { "Could not read bundle from classpath: $absolutePath" }
            .map { Json.decodeFromStream(FeatureBundle.serializer(), it) }
            .map { it.toFeatures() }
    } catch (e: Exception) {
        "Failed to read bundle from classpath: $absolutePath: ${e.message}".asFailure()
    }

    override fun get() = bundle
    override fun close() {}

    override val readyFuture: CompletableFuture<FeatureSource> = bundle
        .map { CompletableFuture.completedFuture<FeatureSource>(this) }
        .recover { CompletableFuture<FeatureSource>().apply {
            completeExceptionally(IllegalStateException(it))
        } }
}