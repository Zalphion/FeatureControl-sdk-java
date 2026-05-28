package com.zalphion.featurecontrol;

import lombok.val;

public class Main {
    public static void main(String[] args) {

        /*
         * Step 1: Init FeatureSource
         * Using an embedded API key to pull features from the North America cloud.
         * Pre-fetching and periodically refreshing for non-blocking operation.
         */
        val features = FeatureControl.northAmerica()
                .toFeatureSource(System.getenv("SDK_KEY"))
                .preFetching();

        /*
         * Step 2: Configure Application
         * Create feature flags and type property references.
         * Inject them into your application logic; they will always be up to date with the FeatureSource.
         */
        val module = BusinessModule.builder()
                .dashboardFlag(features.flag("business-module", "off"))
                .excitementLevel(features.property("excitement-level", Integer::parseInt, 1))
                .build();

        /*
         * Step 3: Start Application
         */
        // start server
    }
}
