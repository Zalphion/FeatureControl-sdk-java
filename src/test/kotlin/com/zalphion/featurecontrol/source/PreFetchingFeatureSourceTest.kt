package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.Features
import com.zalphion.featurecontrol.stringProperty
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import dev.forkhandles.time.DeterministicScheduler
import java.time.Duration

private fun buildFeatures(propValue: String) = Features(
    properties = mapOf("prop" to { propValue }),
    flags = emptyMap()
)

class PreFetchingFeatureSourceTest {

    private val scheduler = DeterministicScheduler()
    private var invocations = 0
    private var nextResult: Result4k<Features, String> = buildFeatures("foo").asSuccess()

    private val source = FeatureSource.memory { invocations++; nextResult }.preFetching(
        refreshInternal = Duration.ofMinutes(1),
        retryInterval = Duration.ofSeconds(1),
        scheduler = scheduler
    )

    private val property = source.stringProperty("prop", default = "default")

    @Test
    fun `caches bundle`() {
        scheduler.tick(Duration.ZERO)

        source.get().shouldBeSuccess()
        source.get().shouldBeSuccess()
        invocations shouldBe 1
    }

    @Test
    fun `refreshes after delay`() {
        scheduler.tick(Duration.ZERO)

        property.get().also { invocations shouldBe 1 } shouldBe "foo"

        nextResult = buildFeatures("bar").asSuccess()
        property.get().also { invocations shouldBe 1 } shouldBe "foo"

        scheduler.tick(Duration.ofSeconds(40))
        property.get().also { invocations shouldBe 1 } shouldBe "foo"

        scheduler.tick(Duration.ofSeconds(40))
        property.get().also { invocations shouldBe 2 } shouldBe "bar"
    }

    @Test
    fun `can gracefully handle a failing bundle`() {
        nextResult = "foo".asFailure()
        scheduler.tick(Duration.ZERO)
        invocations shouldBe 1

        property.get() shouldBe "default"
    }

    @Test
    fun `retries if bundle fails before ready`() {
        nextResult = "foo".asFailure()

        scheduler.tick(Duration.ZERO)
        property.get() shouldBe "default"
        invocations shouldBe 1

        scheduler.tick(Duration.ofSeconds(1))
        property.get() shouldBe "default"
        invocations shouldBe 2

        nextResult = buildFeatures("foo").asSuccess()

        scheduler.tick(Duration.ofSeconds(1))
        property.get() shouldBe "foo"
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
    fun `close source`() {
        scheduler.tick(Duration.ZERO)
        property.get() shouldBe "foo"

        source.close()
        property.get() shouldBe "foo"

        nextResult = buildFeatures("bar").asSuccess()
        scheduler.tick(Duration.ofMinutes(5))
        property.get() shouldBe "foo"
    }
}