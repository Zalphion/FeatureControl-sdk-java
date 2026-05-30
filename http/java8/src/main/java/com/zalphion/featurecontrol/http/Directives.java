package com.zalphion.featurecontrol.http;

import lombok.*;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
class Directives {
    private final Map<String, String> directives;

    public static @NonNull Directives parse(@NonNull @lombok.NonNull String header) {
        val directives = new HashMap<String, String>();

        for (String token: header.split(",")) {
            val parts = token.trim().split("=");

            switch(parts.length) {
                case 1:
                    directives.put(parts[0], null);
                    continue;
                case 2:
                    directives.put(parts[0], parts[1]);
            }
        }

        return new Directives(directives);
    }

    public @NonNull Optional<String> get(@NonNull @lombok.NonNull String name) {
        return directives.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .findFirst().map(Map.Entry::getValue);
    }

    public @NonNull Optional<Integer> getInt(@NonNull @lombok.NonNull String name) {
        return get(name).map(value -> {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }
}