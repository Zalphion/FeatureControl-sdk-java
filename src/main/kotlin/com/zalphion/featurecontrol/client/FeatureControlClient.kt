package com.zalphion.featurecontrol.client

import com.zalphion.featurecontrol.FeatureBundle
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect.NEVER
import java.net.http.HttpClient.Version.HTTP_1_1
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import kotlin.jvm.optionals.getOrElse

class FeatureControlClient(
    private val baseUri: URI,
    private val client: HttpClient = HttpClient.newBuilder()
        .version(HTTP_1_1)
        .followRedirects(NEVER)
        .build()
) {
    companion object {
        val canada = FeatureControlClient(URI("https://ca.featurecontrol.app"))
        val europe = FeatureControlClient(URI("https://eu.featurecontrol.app"))
        val usa = FeatureControlClient(URI("https://us.featurecontrol.app"))
        val oceania = FeatureControlClient(URI("https://oc.featurecontrol.app"))
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun getBundle(sdkKey: String, ifNoneMatch: String? = null): Result4k<BundleResponse, String> {
        val request = HttpRequest.newBuilder().run {
            GET()
            uri(baseUri.resolve("/api/sdk_v1/bundle"))
            header("Authorization", "Bearer $sdkKey")
            if (ifNoneMatch != null) header("If-None-Match", ifNoneMatch)
            build()
        }

        return try {
            val response = client.send(request, BodyHandlers.ofInputStream())
            when (response.statusCode()) {
                200 -> TaggedSdkBundle(
                    bundle = response.body().use { Json.decodeFromStream(FeatureBundle.serializer(), it) },
                    eTag = response.eTag()
                ).asSuccess()
                304 -> Success(BundleStillCurrent(response.eTag()))
                401 -> "Invalid SDK Key".asFailure()
                403 -> "Your key has been banned for abuse.  Please review the fair-use documentation".asFailure()
                429 -> "Rate limit temporarily exceeded.  Please review the fair-use documentation".asFailure()
                else -> "Unexpected status code: ${response.statusCode()}".asFailure()
            }
        } catch (e: Exception) {
            Failure(e.message ?: "Unknown error")
        }
    }
}

private fun HttpResponse<*>.eTag() = headers().firstValue("ETag").getOrElse { error("Missing ETag header from response") }