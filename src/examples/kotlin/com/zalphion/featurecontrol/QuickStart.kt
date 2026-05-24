package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.client.FeatureControl
import com.zalphion.featurecontrol.source.FeatureSource
import com.zalphion.featurecontrol.source.http
import com.zalphion.featurecontrol.source.preFetching

fun main() {
    /*
     * Build a FeatureFlags instance from the cloud provider.
     * The pre-fetching wrapper will cache the latest data and periodically refresh it.
     *
     * Fetching occurs in the background, but you can block on the `FeatureSource` for readiness.
     * This is not recommended for production servers, as it may stall the application.
     */
    val features = FeatureSource
        .http(FeatureControl.northAmerica, System.getenv("FEATURE_CONTROL_SDK_KEY"))
        .preFetching()

    /*
     * You can define a property or flag on init and share it within your application;
     * this is the idiomatic way to use FeatureControl.
     */
    val greetingProperty = features.stringProperty("greeting", default = "hello")
    val myFeatureFlag = features.flag("my-feature", defaultVariant = "off")

    /*
     * Get the latest property value.
     * If the features are not yet ready, the default value is returned.
     */
    println(greetingProperty.get())

    /*
     * Evaluate the flag for a recipient.
     * Different recipients may result in different variants, based on your remote configuration.
     * You must always provide a default in case the flag is not defined.
     */
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