package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.bundle1
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class InMemoryFeatureFlagsTest {

    @Test
    fun `getBundle - static`() {
        FeatureFlags.memory(bundle1).getBundle() shouldBeSuccess bundle1
    }

    @Test
    fun `ready immediately`() {
        FeatureFlags.memory(bundle1).readyFuture.isDone shouldBe true
    }

    @Test
    fun `getBundle - dynamic`() {
        var bundle = bundle1

        val flags = FeatureFlags.memory { bundle }
        flags.getBundle() shouldBeSuccess bundle

        bundle = bundle.copy(properties = mapOf("str" to "bar"))
        flags.getBundle() shouldBeSuccess bundle
    }

    @Test
    fun `closing has no effect`() {
        val flags = FeatureFlags.memory(bundle1)
        flags.readyFuture.isDone shouldBe true
        flags.close()
        flags.getBundle() shouldBeSuccess bundle1
    }
}