package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.source.FeatureSource
import com.zalphion.featurecontrol.source.memory
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FeatureFlagTest {

    private val flags = FeatureSource.memory(testBundle.toFeatures())
    private val lasers = flags.flag("lasers", defaultVariant = "off")

    @Test
    fun `get variant - from override`() {
        lasers.getVariant("user1") shouldBe "on"
        lasers.getVariant("user2") shouldBe "off"
    }

    @Test
    fun `get variant - flag not found`() {
        flags.flag("missing", defaultVariant = "off")
            .getVariant("user1") shouldBe "off"
    }

    @Test
    fun `get variant - bundle error`() {
        FeatureSource.memory("failure")
            .flag("lasers", defaultVariant = "off")
            .getVariant("user1") shouldBe "off"
    }

    @Test
    fun `get variant - bundle exception`() {
        FeatureSource.memory { error("catastrophe!") }
            .flag("lasers", defaultVariant = "off")
            .getVariant("user1") shouldBe "off"
    }

    @Test
    fun `get variant - from bucketing`() {
        lasers.getVariant("user3") shouldBe "off"
        lasers.getVariant("user4") shouldBe "on"
    }

    @Test
    fun `get variant - empty buckets`() {
        flags.flag("treats", defaultVariant = "copious").getVariant("toggles") shouldBe "copious"
    }

    @Test
    fun `get variant - bucket should be sticky`() {
        var offThreshold = 2
        var onThreshold = 8

        val lasers = FeatureSource.memory(
            Features(
                properties = emptyMap(),
                flags = mapOf(
                    "lasers" to { recipient -> "foo" }
                )
            )
        ).flag("lasers", defaultVariant = "off")

        val offSubjects = 0.rangeTo(1_000)
            .filter { lasers.getVariant("user$it") == "off" }

        offThreshold = 4
        onThreshold = 8

        0.rangeTo(1_000)
            .filter { lasers.getVariant("user$it") == "off" }
            .shouldContainAll(offSubjects)
    }
}