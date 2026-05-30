package com.zalphion.featurecontrol.http;

import org.jspecify.annotations.NonNull;

@FunctionalInterface
public interface HttpFunction {
    @NonNull @lombok.NonNull HttpResponse exchange(@NonNull @lombok.NonNull HttpRequest request);
}
