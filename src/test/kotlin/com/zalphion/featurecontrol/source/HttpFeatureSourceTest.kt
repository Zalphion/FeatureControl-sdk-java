package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.client.FakeServer
import com.zalphion.featurecontrol.client.FeatureControl
import com.zalphion.featurecontrol.testBundle
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Instant

class HttpFeatureSourceTest {

    private var time = Instant.parse("2026-01-01T12:00:00Z")
    private val server = FakeServer("key1" to testBundle) { time }

    @AfterEach
    fun cleanup() {
        server.stop()
    }

    private val client = FeatureControl(URI("http://localhost:${server.start()}"))

    @Test
    fun `get - unauthorized`() {
        client.toFeatureSource("missingKey").get() shouldBeFailure "Invalid SDK Key"
        server.getResponses().shouldContainExactly("missingKey" to 401)
    }

    @Test
    fun `get - present`() {
        client.toFeatureSource("key1").get().shouldBeSuccess()
        server.getResponses().shouldContainExactly("key1" to 200)
    }

    @Test
    fun `get - cached`() {
        val flags = client.toFeatureSource("key1")

        flags.get().shouldBeSuccess()
        flags.get().shouldBeSuccess()
        server.getResponses().shouldContainExactly("key1" to 200, "key1" to 304)
    }
}