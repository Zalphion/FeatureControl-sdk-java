package com.zalphion.featurecontrol.bundle;

import com.zalphion.featurecontrol.lib.Failure;
import com.zalphion.featurecontrol.lib.Result;
import com.zalphion.featurecontrol.lib.Success;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.val;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.CRC32;

@Data
@Builder
public class FlagDefinition {
    private final @Singular @NonNull Map<@NonNull String, @NonNull String> overrides;
    private final @Singular @NonNull List<@NonNull VariantDefinition> buckets;
    private final @NonNull String saltBase64;

    public Result<String> evaluate(
            @NonNull @lombok.NonNull String recipient,
            @NonNull @lombok.NonNull Function<@NonNull @lombok.NonNull String, @NonNull @lombok.NonNull String> getDefaultVariant
    ) {
        val overrideValue = overrides.get(recipient);
        if (overrideValue != null) return new Success<>(overrideValue);

        if (buckets.isEmpty()) return new Failure<>("Buckets are empty");

        final long hash;
        {
            val hashFunction = new CRC32();
            hashFunction.update(recipient.getBytes(StandardCharsets.UTF_8));
            hashFunction.update(Base64.getDecoder().decode(saltBase64));

            val modulo = buckets.get(buckets.size() - 1).getThreshold();
            hash = hashFunction.getValue() % modulo;
        }

        return buckets.stream()
                .filter(bucket -> hash < bucket.getThreshold())
                .findAny()
                .map(bucket -> (Result<String>) new Success<>(bucket.getName()))
                .orElseGet(() -> new Failure<>("No matching variant bucket found"));
    }

}
