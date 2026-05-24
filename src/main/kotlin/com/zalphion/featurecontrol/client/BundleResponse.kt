package com.zalphion.featurecontrol.client

import com.zalphion.featurecontrol.FeatureBundle

internal sealed interface BundleResponse

internal data class TaggedSdkBundle(
    val bundle: FeatureBundle,
    val eTag: String
): BundleResponse

internal data class BundleStillCurrent(
    val eTag: String
): BundleResponse