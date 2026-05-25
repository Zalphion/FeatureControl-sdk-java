package com.zalphion.featurecontrol.client

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import com.zalphion.featurecontrol.FeatureBundle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.net.InetSocketAddress
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

internal class FakeServer(
    private vararg val bundles: Pair<String, FeatureBundle>,
    private val clock: () -> Instant
) {

    private val httpDateFormatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC)
    private val responses = mutableListOf<Pair<String, Int>>()
    fun getResponses() = responses.toList()

    private val server = HttpServer.create(InetSocketAddress(0), 1000).apply {
        createContext("/") { exchange ->
            if (exchange.requestMethod == "GET" && exchange.requestURI.path == "/sdkapi/v1/bundle") {
                getBundle(exchange)
            } else {
                exchange.sendResponseHeaders(404, -1)
            }
        }
        executor = Executors.newSingleThreadExecutor()
    }

    fun start(): Int {
        server.start()
        return server.address.port
    }
    fun stop() = server.stop(0)

    @OptIn(ExperimentalSerializationApi::class)
    private fun getBundle(exchange: HttpExchange) {
        val ifNoneMatch = exchange.requestHeaders["If-None-Match"]?.firstOrNull()
        val sdkKey = exchange.requestHeaders["Authorization"]?.firstOrNull()?.split(" ")?.last()

        exchange.responseHeaders.set("Date", httpDateFormatter.format(clock()))

        val bundle = bundles.find { it.first == sdkKey }?.second ?: run {
            exchange.sendResponseHeaders(401, -1)
            responses += sdkKey.orEmpty() to 401
            return
        }

        val eTag = "W/\"${bundle.hashCode()}\""
        exchange.responseHeaders.set("ETag", eTag)

        if (eTag == ifNoneMatch) {
            exchange.sendResponseHeaders(304, -1)
            responses += sdkKey.orEmpty() to 304
            return
        }

        // length of 0 to indicate chunked transfer encoding
        exchange.sendResponseHeaders(200, 0)
        exchange.responseBody.use {
            Json.encodeToStream(FeatureBundle.serializer(), bundle, it)
        }
        responses += sdkKey.orEmpty() to 200
    }
}