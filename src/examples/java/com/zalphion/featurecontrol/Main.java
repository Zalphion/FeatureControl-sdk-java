package com.zalphion.featurecontrol;

import com.zalphion.featurecontrol.source.ApplicationSource;

import java.net.URI;

public class Main {
    public static void main(String[] args) {

        /*
         * Step 1: Init FeatureSource
         * Using an embedded API key to pull features from a self-hosted Feature Control instance.
         * Pre-fetching and periodically refreshing for non-blocking operation.
         */
        final ApplicationSource source = new FeatureControl(URI.create("http://featurecontrol.internal"))
                .toFeatureSource(System.getenv("FEATURE_CONTROL_SDK_KEY"))
                .preFetching();

        /*
         * Step 2: Configure your Application
         * Create feature flags and property references.
         * Inject them into your application; they will always be up to date with the FeatureSource.
         */
        final BusinessModule module = BusinessModule.builder()
                .dashboardFlag(source.flag("dashboard", "off"))
                .excitementLevel(source.property("excitement", Integer::parseInt, 1))
                .build();

        /*
         * Step 3: Start Application
         */
        // start server
    }
}
