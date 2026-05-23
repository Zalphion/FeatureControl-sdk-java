package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.bundle1
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import java.util.concurrent.ExecutionException

class ClasspathFeatureFlagsTest {

    @Test
    fun `ready immediately if resource found`() {
        FeatureFlags.classpath("com/zalphion/featurecontrol/bundle1.json").readyFuture.get()
    }

    @Test
    fun `completes exceptionally if resource not found`() {
        shouldThrow<ExecutionException> {
            FeatureFlags.classpath("com/zalphion/featurecontrol/missing.json").readyFuture.get()
        }
    }

    @Test
    fun `get - missing`() {
        FeatureFlags.classpath("com/zalphion/featurecontrol/missing.json").getBundle()
            .shouldBeFailure("Could not read bundle from classpath: com/zalphion/featurecontrol/missing.json")
    }

    @Test
    fun `get - present`() {
        FeatureFlags.classpath("com/zalphion/featurecontrol/bundle1.json").getBundle() shouldBeSuccess bundle1
    }

    @Test
    fun `closing has no effect`() {
        val flags = FeatureFlags.classpath("com/zalphion/featurecontrol/bundle1.json")
        flags.close()
        flags.getBundle() shouldBeSuccess bundle1
    }
}