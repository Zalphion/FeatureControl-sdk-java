package com.zalphion.featurecontrol.lib.http;

import org.jspecify.annotations.NonNull;

@FunctionalInterface
public interface HttpFunction {
    @NonNull @lombok.NonNull HttpResponse exchange(@NonNull @lombok.NonNull HttpRequest request);

    default boolean hasCache() {
        return false;
    }
}
