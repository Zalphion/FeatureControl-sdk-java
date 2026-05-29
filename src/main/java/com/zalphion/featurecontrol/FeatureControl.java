package com.zalphion.featurecontrol;

import com.zalphion.featurecontrol.bundle.FeatureBundle;
import com.zalphion.featurecontrol.bundle.FeatureBundleSerializer;
import com.zalphion.featurecontrol.lib.http.*;
import com.zalphion.featurecontrol.lib.http.cache.CacheAwareHttpFunction;
import com.zalphion.featurecontrol.lib.result.Failure;
import com.zalphion.featurecontrol.lib.result.Result;
import com.zalphion.featurecontrol.source.FeatureSource;
import lombok.val;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

public class FeatureControl {
    private final @NonNull URI baseUri;
    private final @NonNull HttpFunction http;

    public FeatureControl(@NonNull @lombok.NonNull URI baseUri) {
        this(baseUri, HttpUrlConnectionHttpFunction.builder().build());
    }

    public FeatureControl(@NonNull @lombok.NonNull URI baseUri, @NonNull @lombok.NonNull HttpFunction http) {
        this.baseUri = baseUri;

        // enforce cache awareness
        if (http instanceof CacheAwareHttpFunction) {
            this.http = http;
        } else {
            this.http = new CacheAwareHttpFunction(http);
        }
    }

    public static @lombok.NonNull FeatureControl northAmerica() {
        return new FeatureControl(URI.create("https://na.featurecontrol.app"));
    }

    public static @lombok.NonNull FeatureControl europe() {
        return new FeatureControl(URI.create("https://eu.featurecontrol.app"));
    }

    public static @lombok.NonNull FeatureControl asiaPacific() {
        return new FeatureControl(URI.create("https://ap.featurecontrol.app"));
    }

    public @NonNull Result<FeatureBundle> getBundle(@NonNull @lombok.NonNull String sdkKey) {
        try {
            val request = HttpRequest.builder()
                    .method(HttpMethod.GET)
                    .url(baseUri.resolve("/sdkapi/v1/bundle").toURL())
                    .header("Authorization", Collections.singletonList("Bearer " + sdkKey))
                    .build();

            val response = http.exchange(request);

            switch(response.getStatusCode()) {
                case 200:
                    val json = new String(response.getBody(), StandardCharsets.UTF_8);
                    return FeatureBundleSerializer.fromJson(json);
                case 401: return new Failure<>("Invalid SDK Key");
                case 403: return new Failure<>("Application has been banned for abuse");
                case 429: return new Failure<>("Rate limit temporarily exceeded");
                default: return new Failure<>("Unexpected status code " + response.getStatusCode() + ": " + response.getStatusReason());
            }
        } catch (IOException e) {
            return new Failure<>(Optional.ofNullable(e.getMessage()).orElse("Unknown error"));
        }
    }

    public @NonNull FeatureSource toFeatureSource(@NonNull @lombok.NonNull String sdkKey) {
        return new FeatureSource() {
            @Override
            protected @NonNull @lombok.NonNull Result<FeatureBundle> getInternal() {
                return getBundle(sdkKey);
            }
        };
    }
}