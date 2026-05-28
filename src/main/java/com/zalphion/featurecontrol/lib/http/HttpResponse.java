package com.zalphion.featurecontrol.lib.http;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class HttpResponse implements HttpMessage {
    private final int statusCode;
    private final @NonNull @lombok.NonNull String statusReason;
    private final @Singular @NonNull @lombok.NonNull Map<String, List<String>> headers;
    private final @Builder.Default byte[] body = new byte[0];
}
