package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.source.FeatureSource
import com.zalphion.featurecontrol.source.memory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException

class PropertyTest {

    private val bundleFn = FeatureSource.memory(testBundle.toFeatures())

    @Test
    fun `missing property`() {
        bundleFn.stringProperty("missing", "default").get() shouldBe "default"
    }

    @Test
    fun `found property`() {
        bundleFn.property("str", "default", onFailure = { fail(it) }, coerceFn = { it }).get() shouldBe "foo"
    }

    @Test
    fun `string property`() {
        bundleFn.stringProperty("str", "default").get() shouldBe "foo"
    }

    @Test
    fun `boolean property`() {
        bundleFn.booleanProperty("bool", false).get() shouldBe true
    }

    @Test
    fun `boolean property - malformed`() {
        shouldThrow<IllegalArgumentException> {
            bundleFn.booleanProperty("str", false).get()
        }
    }

    @Test
    fun `int property`() {
        bundleFn.intProperty("int", 0).get() shouldBe 42
    }

    @Test
    fun `int property - malformed`() {
        shouldThrow<NumberFormatException> {
            bundleFn.intProperty("str", 0).get() shouldBe 42
        }
    }

    @Test
    fun `long property`() {
        bundleFn.longProperty("int", 0L).get() shouldBe 42L
    }

    @Test
    fun `double property`() {
        bundleFn.doubleProperty("decimal", 0.0).get() shouldBe 3.14
    }

    @Test
    fun `float property`() {
        bundleFn.floatProperty("decimal", 0.0f).get() shouldBe 3.14f
    }

    @Test
    fun `big decimal property`() {
        bundleFn.bigDecimalProperty("decimal", BigDecimal.ZERO).get() shouldBe BigDecimal("3.14")
    }

    @Test
    fun `big integer property`() {
        bundleFn.bigIntProperty("int", 0.toBigInteger()).get() shouldBe 42.toBigInteger()
    }

    @Test
    fun `base64 property`() {
        bundleFn.base64Property("base64", "foo".encodeToByteArray()).get().decodeToString() shouldBe "lolcats"
    }

    @Test
    fun `instant property`() {
        bundleFn.instantProperty("instant", Instant.EPOCH).get() shouldBe Instant.parse("2026-01-01T12:00:00Z")
    }

    @Test
    fun `instant property - malformed`() {
        shouldThrow<DateTimeParseException> {
            bundleFn.instantProperty("str", Instant.EPOCH).get()
        }
    }

    @Test
    fun `duration property`() {
        bundleFn.durationProperty("duration", Duration.ZERO).get() shouldBe Duration.ofHours(1)
    }

    @Test
    fun `duration property - malformed`() {
        shouldThrow<DateTimeParseException> {
            bundleFn.durationProperty("str", Duration.ZERO).get()
        }
    }

    private enum class TestEnum { Foo, Bar, Baz }

    @Test
    fun `enum property`() {
        bundleFn.enumProperty("str2", TestEnum.Baz).get() shouldBe TestEnum.Foo
    }

    @Test
    fun `enum property - ignore case`() {
        bundleFn.enumProperty("str", TestEnum.Baz, ignoreCase = true).get() shouldBe TestEnum.Foo
    }

    @Test
    fun `enum property - unsupported value`() {
        shouldThrow<NoSuchElementException> {
            bundleFn.enumProperty("str", TestEnum.Baz).get()
        }
    }
}