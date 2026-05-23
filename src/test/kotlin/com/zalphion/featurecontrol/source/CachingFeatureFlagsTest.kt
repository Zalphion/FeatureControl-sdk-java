package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.FeatureBundle
import com.zalphion.featurecontrol.bundle1
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

class CachingFeatureFlagsTest {

    private var time = Instant.parse("2026-01-01T12:00:00Z")
    private var invocations = 0
    private var nextResult: Result4k<FeatureBundle, String> = bundle1.asSuccess()
    private val flags = object: FeatureFlags {
        override fun getBundle() = nextResult.also { invocations++ }
        override val readyFuture: CompletableFuture<FeatureFlags> = CompletableFuture.completedFuture(this)
        override fun close() { }
    }.caching(
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
        val future = CompletableFuture<FeatureFlags>()
        val flags = object: FeatureFlags {
            override fun getBundle() = nextResult
            override fun close() {}
            override val readyFuture: CompletableFuture<FeatureFlags> = future
        }.caching()

        flags.readyFuture.isDone shouldBe false
        future.complete(flags)
        flags.readyFuture.isDone shouldBe true
    }

    @Test
    fun `blocks on get`() {
        invocations shouldBe 0
        flags.getBundle() shouldBeSuccess bundle1
        invocations shouldBe 1
    }

    @Test
    fun `caches bundle`() {
        flags.getBundle() shouldBeSuccess bundle1
        invocations shouldBe 1

        nextResult = "foo".asFailure()

        time += Duration.ofSeconds(30)
        flags.getBundle() shouldBeSuccess bundle1
        invocations shouldBe 1

        time += Duration.ofSeconds(30)
        flags.getBundle() shouldBeFailure "foo"
        invocations shouldBe 2
    }
}