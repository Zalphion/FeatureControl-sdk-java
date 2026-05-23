package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.source.FeatureFlags
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peekFailure
import dev.forkhandles.result4k.recover
import java.util.zip.CRC32

interface FeatureFlag {
    val name: String

    fun getVariant(recipient: String): String
}

fun FeatureFlags.flag(name: String, defaultVariant: String, onFailure: (String) -> Unit = ::println) =
    flag(name, { defaultVariant }, onFailure)

/**
 * @param defaultFn: exchange a recipient for a default variant
 */
fun FeatureFlags.flag(
    name: String,
    defaultFn: (String) -> String,
    onFailure: (String) -> Unit = ::println
) = object: FeatureFlag {
    override val name = name
    override fun getVariant(recipient: String): String = getBundle()
        .flatMap { it.flags[name].asResultOr { "flag $name not found" } }
        .map { it.evaluate(recipient) }
        .peekFailure(onFailure)
        .recover { defaultFn(recipient) }
}

private fun FlagBundle.evaluate(recipient: String): String {
    overrides[recipient]?.let { return it }

    val hash = CRC32().run {
        update(recipient.encodeToByteArray() + saltHex.hexToByteArray())
        (value % modulo).toInt()
    }

    return buckets.find { hash < it.threshold }?.name ?: default
}