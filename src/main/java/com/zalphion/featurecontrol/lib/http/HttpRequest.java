package com.zalphion.featurecontrol.lib.http;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class HttpRequest implements HttpMessage {
    private final @Builder.Default @NonNull @lombok.NonNull HttpMethod method = HttpMethod.GET;
    private final @NonNull @lombok.NonNull URL url;
    private final @Singular @NonNull @lombok.NonNull Map<String, List<String>> headers = Collections.emptyMap();
    private final @Builder.Default byte[] body = new byte[0];
}
