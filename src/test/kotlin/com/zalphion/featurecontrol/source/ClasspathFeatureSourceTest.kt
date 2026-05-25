package com.zalphion.featurecontrol.source

import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import org.junit.jupiter.api.Test

class ClasspathFeatureSourceTest {

    @Test
    fun `get - missing`() {
        FeatureSource.classpath("com/zalphion/featurecontrol/missing.json").get()
            .shouldBeFailure("Could not read bundle from classpath: com/zalphion/featurecontrol/missing.json")
    }

    @Test
    fun `get - present`() {
        FeatureSource.classpath("com/zalphion/featurecontrol/bundle1.json").get().shouldBeSuccess()
    }
}