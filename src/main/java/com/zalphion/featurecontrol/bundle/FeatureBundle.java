package com.zalphion.featurecontrol.bundle;


import lombok.Data;
import org.jspecify.annotations.NonNull;

import java.util.Map;

@Data
public class FeatureBundle {
    private final @NonNull Map<@NonNull String, String> properties;
    private final Map<@NonNull @lombok.NonNull String, FlagBundle> flags;
}