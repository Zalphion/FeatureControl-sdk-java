package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.Features
import com.zalphion.featurecontrol.stringProperty
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

class PreFetchingFeatureSourceTest {

    private var value = "foo"
    private val features = Features(
        properties = mapOf("str" to { value }),
        flags = emptyMap()
    )

    private val scheduler = DeterministicScheduler()
    private var invocations = 0
    private var nextResult: Result4k<Features, String> = features.asSuccess()

    private val source = FeatureSource.memory { invocations++; nextResult }.preFetching(
        refreshInternal = Duration.ofMinutes(1),
        retryInterval = Duration.ofSeconds(1),
        scheduler = scheduler
    )

    private val property = source.stringProperty("prop", default = "default")

    @Test
    fun `ready after fetch`() {
        source.readyFuture.isDone shouldBe false
        source.get() shouldBeFailure "FeatureFlags is not yet ready"

        scheduler.tick(Duration.ZERO)
        source.readyFuture.isDone shouldBe true
        source.get().shouldBeSuccess()
    }

    @Test
    fun `not ready if underlying isn't ready`() {
        val future = CompletableFuture<FeatureSource>()
        val flags = object: FeatureSource {
            override fun get() = nextResult.also { invocations++ }
            override fun close() {}
            override val readyFuture: CompletableFuture<FeatureSource> = future
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
        source.readyFuture.get()

        source.get().shouldBeSuccess()
        source.get().shouldBeSuccess()
        invocations shouldBe 1
    }

    @Test
    fun `refreshes after delay`() {
        scheduler.tick(Duration.ZERO)
        property.get() shouldBe "foo"

        value = "bar"
        property.get() shouldBe "foo"

        scheduler.tick(Duration.ofSeconds(40))
        property.get() shouldBe "foo"

        scheduler.tick(Duration.ofSeconds(40))
        property.get() shouldBe "bar"
    }

    @Test
    fun `not ready if getBundle fails`() {
        nextResult = "foo".asFailure()
        scheduler.tick(Duration.ZERO)
        source.readyFuture.isDone shouldBe false
    }

    @Test
    fun `retries if bundle fails before ready`() {
        nextResult = "foo".asFailure()

        scheduler.tick(Duration.ZERO)
        source.readyFuture.isDone shouldBe false
        invocations shouldBe 1

        scheduler.tick(Duration.ofSeconds(1))
        source.readyFuture.isDone shouldBe false
        invocations shouldBe 2

        nextResult = features.asSuccess()

        scheduler.tick(Duration.ofSeconds(1))
        source.readyFuture.isDone shouldBe true
        invocations shouldBe 3
    }

    @Test
    fun `doesn't retry if bundle fails after ready`() {
        scheduler.tick(Duration.ZERO)
        invocations shouldBe 1
        source.get().shouldBeSuccess()

        nextResult = "foo".asFailure()

        scheduler.tick(Duration.ofMinutes(1))
        invocations shouldBe 2
        source.get().shouldBeSuccess()

        scheduler.tick(Duration.ofSeconds(30))
        invocations shouldBe 2
        source.get().shouldBeSuccess()

        scheduler.tick(Duration.ofSeconds(30))
        invocations shouldBe 3
        source.get().shouldBeSuccess()
    }

    @Test
    fun `fails if closed`() {
        scheduler.tick(Duration.ZERO)
        source.readyFuture.get()

        source.close()
        source.get() shouldBeFailure "FeatureFlags is closed"
    }
}