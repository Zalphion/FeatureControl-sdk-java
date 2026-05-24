package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.source.FeatureSource
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

@OptIn(ExperimentalSerializationApi::class)
internal val testBundle = FeatureSource::class.java.classLoader
    .getResourceAsStream("com/zalphion/featurecontrol/bundle1.json")!!
    .use { Json.decodeFromStream(FeatureBundle.serializer(), it) }