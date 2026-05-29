package com.zalphion.featurecontrol.source;

import com.zalphion.featurecontrol.bundle.FeatureBundle;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.time.Clock;
import java.util.Map;

@Builder
public class FakeServer {
    private final @NonNull Clock clock;
    private final @Singular @NonNull Map<String, FeatureBundle> bundles;

}


/*
internal class FakeServer(
    private vararg val bundles: Pair<String, FeatureBundle>,
    private val clock: () -> Instant
) {

    private val httpDateFormatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC)
    private val responses = CopyOnWriteArrayList<Pair<String, Int>>()
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
            responses += sdkKey.orEmpty() to 401
            exchange.sendResponseHeaders(401, -1)
            return
        }

        val eTag = "W/\"${bundle.hashCode()}\""
        exchange.responseHeaders.set("ETag", eTag)

        if (eTag == ifNoneMatch) {
            responses += sdkKey.orEmpty() to 304
            exchange.sendResponseHeaders(304, -1)

            return
        }

        // length of 0 to indicate chunked transfer encoding
        responses += sdkKey.orEmpty() to 200
        exchange.sendResponseHeaders(200, 0)
        exchange.responseBody.use {
            Json.encodeToStream(FeatureBundle.serializer(), bundle, it)
        }
    }
}
 */