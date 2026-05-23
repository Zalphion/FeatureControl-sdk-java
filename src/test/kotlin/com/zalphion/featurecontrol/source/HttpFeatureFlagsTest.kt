package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.bundle1
import com.zalphion.featurecontrol.client.FakeServer
import com.zalphion.featurecontrol.client.FeatureControlClient
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.net.URI

class HttpFeatureFlagsTest {

    private val server = FakeServer("key1" to bundle1)

    @AfterEach
    fun cleanup() {
        server.stop()
    }

    private val client = FeatureControlClient(URI("http://localhost:${server.start()}"))

    @Test
    fun `get - unauthorized`() {
        FeatureFlags.http(client, "missingKey").getBundle() shouldBeFailure "Invalid SDK Key"
        server.getResponses().shouldContainExactly("missingKey" to 401)
    }

    @Test
    fun `ready immediately`() {
        FeatureFlags.http(client, "key1").readyFuture.isDone shouldBe true
    }

    @Test
    fun `get - present`() {
        FeatureFlags.http(client, "key1").getBundle() shouldBeSuccess bundle1
        server.getResponses().shouldContainExactly("key1" to 200)
    }

    @Test
    fun `get - cached`() {
        val flags = FeatureFlags.http(client, "key1")

        flags.getBundle() shouldBeSuccess bundle1
        flags.getBundle() shouldBeSuccess bundle1
        server.getResponses().shouldContainExactly("key1" to 200, "key1" to 304)
    }

    @Test
    fun `closing has no effect`() {
        val flags = FeatureFlags.http(client, "key1")
        flags.readyFuture.isDone shouldBe true
        flags.close()
        flags.getBundle() shouldBeSuccess bundle1
    }
}