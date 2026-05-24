package com.zalphion.featurecontrol.source

import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import java.util.concurrent.ExecutionException

class ClasspathFeatureSourceTest {

    @Test
    fun `ready immediately if resource found`() {
        FeatureSource.classpath("com/zalphion/featurecontrol/bundle1.json").readyFuture.get()
    }

    @Test
    fun `completes exceptionally if resource not found`() {
        shouldThrow<ExecutionException> {
            FeatureSource.classpath("com/zalphion/featurecontrol/missing.json").readyFuture.get()
        }
    }

    @Test
    fun `get - missing`() {
        FeatureSource.classpath("com/zalphion/featurecontrol/missing.json").get()
            .shouldBeFailure("Could not read bundle from classpath: com/zalphion/featurecontrol/missing.json")
    }

    @Test
    fun `get - present`() {
        FeatureSource.classpath("com/zalphion/featurecontrol/bundle1.json").get().shouldBeSuccess()
    }

    @Test
    fun `closing has no effect`() {
        val flags = FeatureSource.classpath("com/zalphion/featurecontrol/bundle1.json")
        flags.close()
        flags.get().shouldBeSuccess()
    }
}