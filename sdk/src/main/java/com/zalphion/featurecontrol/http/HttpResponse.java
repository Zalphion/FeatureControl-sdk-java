package com.zalphion.featurecontrol.http;

import lombok.*;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class HttpResponse implements HttpMessage {
    private final @With int statusCode;
    private final @With @NonNull @lombok.NonNull String statusReason;
    private final @With @Singular @NonNull @lombok.NonNull Map<String, List<String>> headers;
    private final @With @Builder.Default byte[] body = new byte[0];

    @Override
    public @NonNull @lombok.NonNull HttpResponse withHeader(@NonNull @lombok.NonNull String key, @NonNull @lombok.NonNull List<String> values) {
        val headers = new HashMap<>(getHeaders());
        headers.put(key, values);
        return withHeaders(headers);
    }

    @Override
    public @NonNull @lombok.NonNull HttpMessage withHeader(
            @NonNull @lombok.NonNull String key,
            @NonNull @lombok.NonNull String value
    ) {
        return withHeader(key, Collections.singletonList(value));
    }
}
