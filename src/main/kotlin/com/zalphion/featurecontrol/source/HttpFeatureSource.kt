package com.zalphion.featurecontrol.source

import com.zalphion.featurecontrol.client.FeatureControl
import com.zalphion.featurecontrol.toFeatures
import dev.forkhandles.result4k.map

fun FeatureControl.toFeatureSource(
    sdkKey: String
) = object: FeatureSource {
    override fun get() = getBundle(sdkKey).map { it.toFeatures() }
}