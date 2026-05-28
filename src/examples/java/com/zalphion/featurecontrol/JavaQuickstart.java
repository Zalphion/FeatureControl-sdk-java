package com.zalphion.featurecontrol;

import lombok.val;

public class JavaQuickstart {

    public static void main(String[] args) {

//        val prop = Property


        /*
         * Build a FeatureFlags instance from the cloud provider.
         * The pre-fetching wrapper will cache the latest data and periodically refresh it.
         *
         * Fetching occurs in the background, but you can block on the `FeatureSource` for readiness.
         * This is not recommended for production servers, as it may stall the application.
         */
//        val features = JavaFeatureSourceBuilder
//                .http(FeatureControl.getNorthAmerica(), System.getenv("FEATURE_CONTROL_SDK_KEY"))
//                .preFetching(null, null, null);
//
//
//        /*
//         * You can define a property or flag on init and share it within your application;
//         * this is the idiomatic way to use FeatureControl.
//         */
//        val greetingProperty = features.getProperty("greeting", "hello", (value) -> value);
//        val myFeatureFlag = features.getFlag("my-feature", "off");
//
//        /*
//         * Get the latest property value.
//         * If the features are not yet ready, the default value is returned.
//         */
//        System.out.println(greetingProperty.get());
//
//        /*
//         * Evaluate the flag for a recipient.
//         * Different recipients may result in different variants, based on your remote configuration.
//         * You must always provide a default in case the flag is not defined.
//         */
//
//        switch(myFeatureFlag.getVariant("user1")) {
//            case "on":
//                System.out.println("Do cool thing");
//                break;
//            case "both":
//                System.out.println("Do cool thing");
//            case "off":
//                System.out.println("Don't do cool thing");
//                break;
//            default:
//                System.out.println("Unknown variant");
//        }
    }
}
