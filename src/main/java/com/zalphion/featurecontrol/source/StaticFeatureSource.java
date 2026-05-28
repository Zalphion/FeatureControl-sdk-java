package com.zalphion.featurecontrol.source;

import com.zalphion.featurecontrol.BinaryUtils;
import com.zalphion.featurecontrol.bundle.FeatureBundle;
import com.zalphion.featurecontrol.bundle.FeatureBundleSerializer;
import com.zalphion.featurecontrol.lib.result.Result;
import com.zalphion.featurecontrol.lib.result.Success;
import org.jspecify.annotations.NonNull;
import lombok.val;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class StaticFeatureSource extends FeatureSource {
    private final Result<FeatureBundle> result;

    public StaticFeatureSource(@NonNull @lombok.NonNull Result<FeatureBundle> result) {
        this.result = result;
    }

    @Override
    public @NonNull Result<FeatureBundle> getInternal() {
        return result;
    }


    /**
     * Defaults to the Thread Context ClassLoader to ensure resources in the caller's module are visible.
     */
    public static @NonNull FeatureSource fromClasspath(
            @NonNull @lombok.NonNull String absolutePath,
            ClassLoader classLoader
    ) throws IOException {
        val effectiveClassLoader = Optional.ofNullable(classLoader).orElseGet(() -> Thread.currentThread().getContextClassLoader());

        try (val stream = effectiveClassLoader.getResourceAsStream(absolutePath)){
            if (stream == null) throw new IllegalArgumentException("Could not find bundle at " + absolutePath);
            val json = new String(BinaryUtils.readFully(stream), StandardCharsets.UTF_8);

            val bundle = FeatureBundleSerializer.fromJson(json).orElseThrow(IOException::new);
            return new StaticFeatureSource(new Success<>(bundle));
        }
    }
}