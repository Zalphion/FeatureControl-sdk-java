package com.zalphion.featurecontrol.bundle;

import lombok.Data;
import org.jspecify.annotations.NonNull;

@Data
public class VariantBucket {
    private final @NonNull String name;
    private final int threshold;
}
