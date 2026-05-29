package com.zalphion.featurecontrol;

import com.zalphion.featurecontrol.bundle.FeatureBundle;

import java.io.IOException;

public class TestFixtures {

    private TestFixtures() {}

    public static final FeatureBundle bundle1;

    static {
        try {
            bundle1 = FeatureBundle.fromClasspath("com/zalphion/featurecontrol/bundle1.json");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test bundle", e);
        }
    }
}
