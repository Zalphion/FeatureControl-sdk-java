package com.zalphion.featurecontrol

import kotlinx.serialization.Serializable

@Serializable
data class FeatureBundle(
    val flags: Map<String, FlagBundle>,
    val properties: Map<String, String>
)

@Serializable
data class FlagBundle(
    val overrides: Map<String, String>,
    val buckets: List<VariantBucket>,
    val modulo: Int,
    val saltHex: String,
    val default: String,
)

@Serializable
data class VariantBucket(
    val name: String,
    val threshold: Int
)