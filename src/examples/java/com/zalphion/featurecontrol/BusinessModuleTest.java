package com.zalphion.featurecontrol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import lombok.val;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BusinessModuleTest {

    private final Set<String> betaUsers = new HashSet<>(Arrays.asList("admin", "tester"));

    private boolean dashboardEnabled = false;
    private int excitementLevel = 3;

    private final BusinessModule module = BusinessModule.builder()
            .dashboardFlag(FeatureFlag.create(recipient ->
                    betaUsers.contains(recipient) ? "on" : dashboardEnabled ? "on" : "off"
            ))
            .excitementLevel(ApplicationProperty.create(() -> excitementLevel))

            .build();

    @Test
    public void dashboardEnabledForBetaUser() {
        val html = module.renderIndex("admin");

    }

}

