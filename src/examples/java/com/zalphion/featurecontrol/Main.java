package com.zalphion.featurecontrol;

import com.zalphion.featurecontrol.source.FeatureSource;

public class Main {
    public static void main(String[] args) {

        /*
         * Step 1: Init FeatureSource
         * Using an embedded API key to pull features from the North America cloud.
         * Pre-fetching and periodically refreshing for non-blocking operation.
         */
        final FeatureSource features = FeatureControl.northAmerica()
                .toFeatureSource(System.getenv("FEATURE_CONTROL_SDK_KEY"))
                .preFetching();

        /*
         * Step 2: Configure your Application
         * Create feature flags and property references.
         * Inject them into your application; they will always be up to date with the FeatureSource.
         */
        final BusinessModule module = BusinessModule.builder()
                .dashboardFlag(features.flag("dashboard", "off"))
                .excitementLevel(features.property("excitement", Integer::parseInt, 1))
                .build();

        /*
         * Step 3: Start Application
         */
        // start server
    }
}
