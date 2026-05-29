package com.zalphion.featurecontrol.bundle;

import com.zalphion.featurecontrol.BinaryUtils;
import com.zalphion.featurecontrol.source.FeatureSource;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.val;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Data
@Builder
public class FeatureBundle {
    private final @Singular @NonNull Map<@NonNull String, String> properties;
    private final @Singular Map<@NonNull @lombok.NonNull String, FlagBundle> flags;

    public @NonNull FeatureSource toSource() {
        return FeatureSource.create(this);
    }

    public static @NonNull FeatureBundle fromClasspath(
            @NonNull @lombok.NonNull String absolutePath,
            @NonNull @lombok.NonNull ClassLoader classLoader
    ) throws IOException {
        try (val stream = classLoader.getResourceAsStream(absolutePath)){
            if (stream == null) throw new IllegalArgumentException("Could not find bundle at " + absolutePath);
            val json = new String(BinaryUtils.readFully(stream), StandardCharsets.UTF_8);

            return FeatureBundleSerializer.fromJson(json).orElseThrow(IOException::new);
        }
    }

    public static @NonNull FeatureBundle fromClasspath(
            @NonNull @lombok.NonNull String absolutePath
    ) throws IOException {
        return fromClasspath(absolutePath, Thread.currentThread().getContextClassLoader());
    }
}