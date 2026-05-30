/*
 * Portions derived from Http4k
 * Copyright 2026 Http4k Authors
 * Modifications Copyright 2026 Zalphion Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.zalphion.featurecontrol.http;

import com.zalphion.featurecontrol.lib.BinaryUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import lombok.val;

import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The HttpUrlConnection is the most portable HTTP client implementation available in the JDK.
 * But due to its low-level nature, its usage here isn't fully robust.
 * You should consider a more modern implementation.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUrlConnectionHttpFunction implements HttpFunction {
    private final @NonNull Duration readTimeout;
    private final @NonNull Duration connectTimeout;

    public static @NonNull HttpFunction create(
            @NonNull @lombok.NonNull Duration readTimeout,
            @NonNull @lombok.NonNull Duration connectTimeout,
            @NonNull@lombok.NonNull Supplier<Instant> clock
    ) {
        val inner = new HttpUrlConnectionHttpFunction(readTimeout, connectTimeout);
        return new CacheAwareHttpFunction(inner, clock);
    }

    public static @NonNull HttpFunction create() {
        return create(Duration.ofSeconds(10), Duration.ofSeconds(10), Instant::now);
    }

    @Override
    public @NonNull @lombok.NonNull HttpResponse exchange(@NonNull @lombok.NonNull HttpRequest request) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) request.getUrl().openConnection();

            // boilerplate
            connection.setReadTimeout((int) readTimeout.toMillis());
            connection.setConnectTimeout((int) connectTimeout.toMillis());
            connection.setInstanceFollowRedirects(false);
            connection.setDoInput(true);

            connection.setRequestMethod(request.getMethod().toString());
            for (val header : request.getHeaders().entrySet()) {
                for (val value: header.getValue()) {
                    connection.addRequestProperty(header.getKey(), value);
                }
            }

            if (request.getBody().length > 0) {
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(request.getBody().length);

                try (val out = connection.getOutputStream()) {
                    out.write(request.getBody());
                }
            }

            return HttpResponse.builder()
                    .statusCode(connection.getResponseCode())
                    .statusReason(Optional.ofNullable(connection.getResponseMessage()).orElse(""))
                    .headers(
                            // because response status line comes as a header with null key (*facepalm*)
                            connection.getHeaderFields().entrySet().stream()
                                    .filter(header -> header.getKey() != null)
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                    )
                    .body(resolveBody(connection))
                    .build();
        } catch (UnknownHostException e) {
            return HttpResponse.builder().statusCode(503).statusReason("Unknown host").build();
        } catch (ConnectException e) {
            return HttpResponse.builder().statusCode(503).statusReason("Connection refused").build();
        } catch (SocketTimeoutException e) {
            return HttpResponse.builder().statusCode(504).statusReason("Client timeout").build();
        } catch (IOException e) {
            return HttpResponse.builder().statusCode(503).statusReason("Service Unavailable").build();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static byte[] resolveBody(HttpURLConnection connection) throws IOException {
        try (val inputStream = connection.getResponseCode() < 400 ? connection.getInputStream() : connection.getErrorStream()) {
            return inputStream == null ? new byte[0] : BinaryUtils.readFully(inputStream);
        }
    }
}
