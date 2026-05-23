package com.zalphion.featurecontrol.client

import com.zalphion.featurecontrol.FeatureBundle

sealed interface BundleResponse

data class TaggedSdkBundle(
    val bundle: FeatureBundle,
    val eTag: String
): BundleResponse

data class BundleStillCurrent(
    val eTag: String
): BundleResponse