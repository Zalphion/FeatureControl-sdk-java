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
import dev.forkhandles.time.DeterministicScheduler
import java.time.Duration
import java.util.concurrent.CompletableFuture

class PreFetchingFeatureFlagsTest {

    private val scheduler = DeterministicScheduler()
    private var invocations = 0
    private var nextResult: Result4k<FeatureBundle, String> = bundle1.asSuccess()

    private val flags = object: FeatureFlags {
        override fun getBundle() = nextResult.also { invocations++ }
        override fun close() {}
        override val readyFuture: CompletableFuture<FeatureFlags> = CompletableFuture.completedFuture(this)
    }.preFetching(
        refreshInternal = Duration.ofMinutes(1),
        retryInterval = Duration.ofSeconds(1),
        scheduler = scheduler
    )

    @Test
    fun `ready after fetch`() {
        flags.readyFuture.isDone shouldBe false
        flags.getBundle() shouldBeFailure "FeatureFlags is not yet ready"

        scheduler.tick(Duration.ZERO)
        flags.readyFuture.get() shouldBe flags
        flags.getBundle() shouldBeSuccess bundle1
    }

    @Test
    fun `not ready if underlying isn't ready`() {
        val future = CompletableFuture<FeatureFlags>()
        val flags = object: FeatureFlags {
            override fun getBundle() = nextResult.also { invocations++ }
            override fun close() {}
            override val readyFuture: CompletableFuture<FeatureFlags> = future
        }.preFetching(scheduler = scheduler)

        scheduler.tick(Duration.ZERO)
        flags.readyFuture.isDone shouldBe false

        scheduler.tick(Duration.ofMinutes(10))
        flags.readyFuture.isDone shouldBe false

        future.complete(flags)
        scheduler.tick(Duration.ofMinutes(1))
        flags.readyFuture.isDone shouldBe true
    }

    @Test
    fun `caches bundle`() {
        scheduler.tick(Duration.ZERO)
        flags.readyFuture.get()

        flags.getBundle() shouldBeSuccess bundle1
        flags.getBundle() shouldBeSuccess bundle1
        invocations shouldBe 1
    }

    @Test
    fun `refreshes after delay`() {
        scheduler.tick(Duration.ZERO)
        flags.readyFuture.get()

        nextResult = bundle1.copy(properties = mapOf("str" to "bar")).asSuccess()
        flags.getBundle() shouldBeSuccess bundle1

        scheduler.tick(Duration.ofSeconds(40))
        flags.getBundle() shouldBeSuccess bundle1

        scheduler.tick(Duration.ofSeconds(40))
        flags.getBundle() shouldBeSuccess bundle1.copy(properties = mapOf("str" to "bar"))
    }

    @Test
    fun `not ready if getBundle fails`() {
        nextResult = "foo".asFailure()
        scheduler.tick(Duration.ZERO)
        flags.readyFuture.isDone shouldBe false
    }

    @Test
    fun `retries if bundle fails before ready`() {
        nextResult = "foo".asFailure()

        scheduler.tick(Duration.ZERO)
        flags.readyFuture.isDone shouldBe false
        invocations shouldBe 1

        scheduler.tick(Duration.ofSeconds(1))
        flags.readyFuture.isDone shouldBe false
        invocations shouldBe 2

        nextResult = bundle1.asSuccess()

        scheduler.tick(Duration.ofSeconds(1))
        flags.readyFuture.isDone shouldBe true
        invocations shouldBe 3
    }

    @Test
    fun `doesn't retry if bundle fails after ready`() {
        scheduler.tick(Duration.ZERO)
        invocations shouldBe 1
        flags.getBundle() shouldBeSuccess bundle1

        nextResult = "foo".asFailure()

        scheduler.tick(Duration.ofMinutes(1))
        invocations shouldBe 2
        flags.getBundle() shouldBeSuccess bundle1

        scheduler.tick(Duration.ofSeconds(30))
        invocations shouldBe 2
        flags.getBundle() shouldBeSuccess bundle1

        scheduler.tick(Duration.ofSeconds(30))
        invocations shouldBe 3
        flags.getBundle() shouldBeSuccess bundle1
    }

    @Test
    fun `fails if closed`() {
        scheduler.tick(Duration.ZERO)
        flags.readyFuture.get()

        flags.close()
        flags.getBundle() shouldBeFailure "FeatureFlags is closed"
    }
}