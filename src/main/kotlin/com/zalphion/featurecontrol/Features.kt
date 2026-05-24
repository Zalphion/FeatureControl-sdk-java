package com.zalphion.featurecontrol

import java.util.zip.CRC32

class Features(
    val flags: Map<String, GetVariantOrNull>,
    val properties: Map<String, GetProperty>,
) {
    companion object
}

typealias GetVariantOrNull = (recipient: String) -> String?
typealias GetProperty = () -> String

internal fun FeatureBundle.toFeatures() = Features(
    flags = flags.mapValues { (_, flag) -> fromBundle(flag) },
    properties = properties.mapValues { (_, value) -> { value } },
)

private fun fromBundle(bundle: FlagBundle): GetVariantOrNull = fn@{ recipient ->
    bundle.overrides[recipient]?.let { return@fn it }
    val modulo = bundle.buckets.lastOrNull()?.threshold ?: return@fn null

    val hash = CRC32().run {
        update(recipient.encodeToByteArray() + bundle.saltHex.hexToByteArray())
        (value % modulo).toInt()
    }

    // mathematically impossible to not find a result at this point
    bundle.buckets.first { hash < it.threshold }.name
}