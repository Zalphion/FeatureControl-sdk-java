package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.client.FakeServer
import com.zalphion.featurecontrol.client.FeatureControl
import com.zalphion.featurecontrol.testBundle
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.net.URI

class HttpFeatureSourceTest {

    private val server = FakeServer("key1" to testBundle)

    @AfterEach
    fun cleanup() {
        server.stop()
    }

    private val client = FeatureControl(URI("http://localhost:${server.start()}"))

    @Test
    fun `get - unauthorized`() {
        FeatureSource.http(client, "missingKey").get() shouldBeFailure "Invalid SDK Key"
        server.getResponses().shouldContainExactly("missingKey" to 401)
    }

    @Test
    fun `ready immediately`() {
        FeatureSource.http(client, "key1").readyFuture.isDone shouldBe true
    }

    @Test
    fun `get - present`() {
        FeatureSource.http(client, "key1").get().shouldBeSuccess()
        server.getResponses().shouldContainExactly("key1" to 200)
    }

    @Test
    fun `get - cached`() {
        val flags = FeatureSource.http(client, "key1")

        flags.get().shouldBeSuccess()
        flags.get().shouldBeSuccess()
        server.getResponses().shouldContainExactly("key1" to 200, "key1" to 304)
    }

    @Test
    fun `closing has no effect`() {
        val flags = FeatureSource.http(client, "key1")
        flags.readyFuture.isDone shouldBe true
        flags.close()
        flags.get().shouldBeSuccess()
    }
}