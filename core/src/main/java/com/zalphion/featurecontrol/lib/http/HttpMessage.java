package com.zalphion.featurecontrol.lib.http;

import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HttpMessage {
    @NonNull @lombok.NonNull Map<String, List<String>> getHeaders();

    byte[] getBody();

    default @NonNull @lombok.NonNull Optional<List<String>> getHeader(@NonNull @lombok.NonNull String key) {
        return getHeaders().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    default @NonNull @lombok.NonNull Optional<String> getHeaderValue(@NonNull @lombok.NonNull String key) {
        return getHeader(key).flatMap(header -> header.stream().findFirst());
    }

    @NonNull @lombok.NonNull HttpMessage withHeader(
            @NonNull @lombok.NonNull String key,
            @NonNull @lombok.NonNull List<String> values
    );

    @NonNull @lombok.NonNull HttpMessage withHeader(
            @NonNull @lombok.NonNull String key,
            @NonNull @lombok.NonNull String value
    );
}
