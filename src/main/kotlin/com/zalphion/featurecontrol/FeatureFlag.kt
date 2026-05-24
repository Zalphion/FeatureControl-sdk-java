package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.source.FeatureSource
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.peekFailure
import dev.forkhandles.result4k.recover
import org.slf4j.LoggerFactory

interface FeatureFlag {
    val name: String
    fun getVariant(recipient: String): String
}

private val logger by lazy {
    LoggerFactory.getLogger(FeatureFlag::class.java)
}

fun FeatureSource.flag(name: String, defaultVariant: String) = flag(name) { defaultVariant }

/**
 * @param defaultFn: exchange a recipient for a default variant
 */
fun FeatureSource.flag(
    name: String,
    defaultFn: (String) -> String,
) = object: FeatureFlag {
    override val name = name
    override fun getVariant(recipient: String) = safeGet()
        .flatMap { it.flags[name].asResultOr { "flag $name not found" } }
        .flatMap { it(recipient).asResultOr { "flag $name has no variants configured" } }
        .peekFailure(logger::warn)
        .recover { defaultFn(recipient) }
}