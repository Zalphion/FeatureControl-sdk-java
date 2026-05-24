package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.Features
import com.zalphion.featurecontrol.testBundle
import com.zalphion.featurecontrol.toFeatures
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.CompletableFuture

class CachingFeatureSourceTest {

    private var time = Instant.parse("2026-01-01T12:00:00Z")
    private var invocations = 0
    private var nextResult: Result4k<Features, String> = testBundle.toFeatures().asSuccess()
    private val flags = FeatureSource.memory { invocations++; nextResult }.caching(
        ttl = Duration.ofMinutes(1),
        clock = object: Clock() {
            override fun instant() = time
            override fun getZone() = throw NotImplementedError()
            override fun withZone(zone: ZoneId?) = throw NotImplementedError()
        }
    )

    @Test
    fun `ready immediately if underlying is ready`() {
        flags.readyFuture.isDone shouldBe true
    }

    @Test
    fun `not ready if underlying isn't ready`() {
        val future = CompletableFuture<FeatureSource>()
        val flags = object: FeatureSource {
            override fun get() = nextResult
            override fun close() {}
            override val readyFuture: CompletableFuture<FeatureSource> = future
        }.caching()

        flags.readyFuture.isDone shouldBe false
        future.complete(flags)
        flags.readyFuture.isDone shouldBe true
    }

    @Test
    fun `blocks on get`() {
        invocations shouldBe 0
        flags.get().shouldBeSuccess()
        invocations shouldBe 1
    }

    @Test
    fun `caches bundle`() {
        flags.get().shouldBeSuccess()
        invocations shouldBe 1

        nextResult = "foo".asFailure()

        time += Duration.ofSeconds(30)
        flags.get().shouldBeSuccess()
        invocations shouldBe 1

        time += Duration.ofSeconds(30)
        flags.get() shouldBeFailure "foo"
        invocations shouldBe 2
    }
}