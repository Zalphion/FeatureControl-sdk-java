package com.zalphion.featurecontrol.client

import com.zalphion.featurecontrol.FeatureBundle
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URI

/**
 * Will respect all cache headers, including eTag and max-age
 */
class FeatureControl @JvmOverloads constructor(
    private val baseUri: URI,
    cacheDir: File = File.createTempFile("feature-control", "cache").apply { delete(); mkdir() }
) {
    companion object {
        @JvmStatic val northAmerica = FeatureControl(URI("https://na.featurecontrol.app"))
        @JvmStatic val europe = FeatureControl(URI("https://eu.featurecontrol.app"))
        @JvmStatic val asiaPacific = FeatureControl(URI("https://ap.featurecontrol.app"))
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(false)
        .cache(Cache(cacheDir, maxSize = 10L * 1024 * 1024)) // 10 MB cache
        .build()

    @OptIn(ExperimentalSerializationApi::class)
    internal fun getBundle(sdkKey: String): Result4k<FeatureBundle, String> {
        val request = Request.Builder()
            .url(baseUri.resolve("/sdkapi/v1/bundle").toURL())
            .header("Authorization", "Bearer $sdkKey")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                when (response.code) {
                    200 -> response.body.byteStream().use {
                        Json.decodeFromStream(FeatureBundle.serializer(), it)
                    }.asSuccess()
                    401 -> "Invalid SDK Key".asFailure()
                    403 -> "Your key has been banned for abuse.  Please review the fair-use documentation".asFailure()
                    429 -> "Rate limit temporarily exceeded.  Please review the fair-use documentation".asFailure()
                    else -> "Unexpected status code: ${response.code}".asFailure()
                }
            }
        } catch (e: Exception) {
            Failure(e.message ?: "Unknown error")
        }
    }
}