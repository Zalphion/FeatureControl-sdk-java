package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.flag
import com.zalphion.featurecontrol.stringProperty
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class InMemoryFeatureSourceTest {

    private var variant = "foo"
    private var value = "toll"

    private val source = FeatureSource.memory(
        flags = mapOf("flag" to { variant }),
        properties = mapOf("prop" to { value })
    )

    private val flag = source.flag("flag", defaultVariant = "off")
    private val prop = source.stringProperty("prop", default = "default")


    @Test
    fun `ready immediately`() {
        source.readyFuture.isDone shouldBe true
    }

    @Test
    fun `get - dynamic`() {
        flag.getVariant("user1") shouldBe "foo"
        prop.get() shouldBe "toll"

        variant = "bar"
        value = "troll"

        flag.getVariant("user2") shouldBe "bar"
        prop.get() shouldBe "troll"
    }

    @Test
    fun `closing has no effect`() {
        source.readyFuture.isDone shouldBe true
        source.close()
        source.get().shouldBeSuccess()
    }
}