package com.zalphion.featurecontrol

import kotlinx.serialization.Serializable

@Serializable
internal data class FeatureBundle(
    val flags: Map<String, FlagBundle>,
    val properties: Map<String, String>
)

@Serializable
internal data class FlagBundle(
    val overrides: Map<String, String>,
    val buckets: List<VariantBucket>,
    val saltHex: String
)

@Serializable
internal data class VariantBucket(
    val name: String,
    val threshold: Int
)