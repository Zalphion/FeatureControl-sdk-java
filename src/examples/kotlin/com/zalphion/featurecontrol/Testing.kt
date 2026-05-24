package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.source.FeatureSource
import com.zalphion.featurecontrol.source.memory
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.random.Random

class BusinessModule(
    private val random: Random,
    featureSource: FeatureSource,
) {
    private val treatsDoctrine = featureSource.flag("treats-doctrine", defaultVariant = "plenty")

    fun shouldGiveTreats(catId: String): Boolean {
        return when(treatsDoctrine.getVariant(catId)) {
            "copious" -> random.nextInt(10) > 2
            "plenty" -> random.nextInt(10) > 5
            else -> false
        }
    }
}

class FeatureTest {

    private var doctrine = "none"

    private val testObj = BusinessModule(
        random = Random(42),
        featureSource = FeatureSource.memory(
            flags = mapOf("treats-doctrine" to { doctrine })
        )
    )

    private fun calculateDispenseRate(ids: Collection<String>): Float {
        if (ids.isEmpty()) return 0f
        val results = ids.map { testObj.shouldGiveTreats(it) }
        return results.count { it }.toFloat() / results.size
    }

    private val testGroup = 1.rangeTo(1_000).map { "cat$it" }

    @Test
    fun `copious treats`() {
        doctrine = "copious"
        calculateDispenseRate(testGroup) shouldBe (0.683f plusOrMinus 0.05f)
    }

    @Test
    fun `plenty of treats`() {
        doctrine = "plenty"
        calculateDispenseRate(testGroup) shouldBe (0.381f plusOrMinus 0.05f)
    }

    @Test
    fun `no treats`() {
        calculateDispenseRate(testGroup) shouldBe 0f
    }
}