package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.source.FeatureFlags
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peekFailure
import dev.forkhandles.result4k.recover
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.util.Base64

interface Property<Type: Any> {
    val name: String
    fun get(): Type
}

fun <Type: Any> FeatureFlags.property(
    name: String,
    default: Type,
    onFailure: (String) -> Unit = ::println,
    coerceFn: (String) -> Type
) = object: Property<Type> {
    override val name = name
    override fun get() = getBundle()
        .flatMap { it.properties[name].asResultOr { "property $name not found" } }
        .peekFailure(onFailure)
        .map(coerceFn)
        .recover { default }
}

fun FeatureFlags.stringProperty(name: String, default: String) = property(name, default) { it }

fun FeatureFlags.booleanProperty(name: String, default: Boolean) = property(name, default) { text ->
    when(text.lowercase()) {
        "true" -> true
        "false" -> false
        else -> throw IllegalArgumentException("Invalid boolean: $text")
    }
}

inline fun <reified EnumType: Enum<EnumType>> FeatureFlags.enumProperty(
    name: String,
    default: EnumType,
    ignoreCase: Boolean = false
) = property(name, default) { text ->
    enumValues<EnumType>().first { it.name.equals(text, ignoreCase = ignoreCase) }
}

fun FeatureFlags.base64Property(
    name: String,
    default: ByteArray,
    decoder: Base64.Decoder = Base64.getDecoder()
) = property(name, default) { decoder.decode(it) }

fun FeatureFlags.intProperty(name: String, default: Int) = property(name, default) { it.toInt() }
fun FeatureFlags.longProperty(name: String, default: Long) = property(name, default) { it.toLong() }
fun FeatureFlags.doubleProperty(name: String, default: Double) = property(name, default) { it.toDouble() }
fun FeatureFlags.floatProperty(name: String, default: Float) = property(name, default) { it.toFloat() }
fun FeatureFlags.bigDecimalProperty(name: String, default: BigDecimal) = property(name, default) { BigDecimal(it) }
fun FeatureFlags.bigIntProperty(name: String, default: BigInteger) = property(name, default) { BigInteger(it) }

fun FeatureFlags.instantProperty(name: String, default: Instant) = property(name, default) { Instant.parse(it) }
fun FeatureFlags.durationProperty(name: String, default: Duration) = property(name, default) { Duration.parse(it) }
