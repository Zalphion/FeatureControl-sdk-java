package com.zalphion.featurecontrol.http;

import lombok.*;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class HttpRequest implements HttpMessage {
    private final @With @Builder.Default @NonNull @lombok.NonNull HttpMethod method = HttpMethod.GET;
    private final @With @NonNull @lombok.NonNull URL url;
    private final @With @Singular @NonNull @lombok.NonNull Map<String, List<String>> headers;
    private final @With @Builder.Default byte[] body = new byte[0];

    @Override
    public @NonNull @lombok.NonNull HttpRequest withHeader(@NonNull @lombok.NonNull String key, @NonNull @lombok.NonNull List<String> values) {
        val headers = new HashMap<>(getHeaders());
        headers.put(key, values);
        return withHeaders(headers);
    }

    @Override
    public @NonNull @lombok.NonNull HttpRequest withHeader(
            @NonNull @lombok.NonNull String key,
            @NonNull @lombok.NonNull String value
    ) {
        return withHeader(key, Collections.singletonList(value));
    }
}
