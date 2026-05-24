package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.client.FeatureControl
import com.zalphion.featurecontrol.source.FeatureSource
import com.zalphion.featurecontrol.source.http
import com.zalphion.featurecontrol.source.preFetching

fun main() {
    /*
     * Build a FeatureFlags instance from the Canada-sovereignty cloud.
     * The pre-fetching wrapper will cache the latest data and periodically refresh it.
     */
    val flags = FeatureSource
        .http(FeatureControl.northAmerica, "my-sdk-key")
        .preFetching()

    /*
     * Get and display a property.
     * get() can be invoked later to reflect the most up-to-date value.
     * If you don't want the latest properties, call get() once on init.
     */
    val greetingProperty = flags.stringProperty("greeting", default = "hello")
    println(greetingProperty.get())

    /*
     * Flags require a recipient, since different recipients may be allocated different variants.
     * You must always provide a default in case the flag is not defined.
     */
    val myFeatureFlag = flags.flag("my-feature", defaultVariant = "off")
    when(myFeatureFlag.getVariant("user1")) {
        "off" -> println("Don't do cool thing")
        "on" -> println("Do cool thing")
        "both" -> {
            println("Don't do cool thing")
            println("Do cool thing")
        }
        else -> println("Unknown variant")
    }
}