package com.zalphion.featurecontrol.source;

import com.zalphion.featurecontrol.FeatureFlag;
import com.zalphion.featurecontrol.FeatureFlagKt;
import com.zalphion.featurecontrol.Property;
import com.zalphion.featurecontrol.PropertyKt;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class JavaFeatureSource {

    @NotNull private final FeatureSource source;

    JavaFeatureSource(@NotNull FeatureSource source) {
        this.source = source;
    }

    public @NotNull FeatureFlag getFlag(
            @NotNull String name,
            @NotNull Function<String, String> recipientToDefaultVariant
    ) {
        return FeatureFlagKt.flag(source, name, recipientToDefaultVariant::apply);
    }

    public @NotNull FeatureFlag getFlag(
            @NotNull String name,
            @NotNull String defaultVariant
    ) {
        return FeatureFlagKt.flag(source, name, defaultVariant);
    }

    public @NotNull <Type> Property<Type> getProperty(
            @NotNull String name,
            @NotNull Type defaultValue,
            @NotNull Function<String, Type> coerceValue
    ) {
        return PropertyKt.property(source, name, defaultValue, coerceValue::apply);
    }
}
