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

import okhttp3.*;
import org.jspecify.annotations.NonNull;
import lombok.val;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

public class OkHttp5HttpFunction implements HttpFunction {
    private final OkHttpClient client;

    public OkHttp5HttpFunction() throws IOException {
        this(Duration.ofSeconds(10), Duration.ofSeconds(10));
    }

    public OkHttp5HttpFunction(
            @NonNull @lombok.NonNull Duration readTimeout,
            @NonNull @lombok.NonNull Duration connectTimeout
    ) throws IOException {
        client = new OkHttpClient.Builder()
                .followRedirects(false)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .cache(new Cache(Files.createTempDirectory("feature-control-okhttp3").toFile(), 1000L))
                .build();
    }

    @Override
    public @NonNull @lombok.NonNull HttpResponse exchange(@NonNull @lombok.NonNull HttpRequest request) {
        val okHttpRequest = new Request.Builder()
                .method(request.getMethod().toString(), request.getBody().length == 0 ? null : RequestBody.create(request.getBody()))
                .url(request.getUrl())
                .headers(Headers.of(request.getHeaders().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.join(", ", entry.getValue())))
                ))
                .build();

        try (val response = client.newCall(okHttpRequest).execute()) {
            return HttpResponse.builder()
                    .statusCode(response.code())
                    .statusReason(response.message())
                    .headers(response.headers().toMultimap())
                    .body(response.body().bytes())
                    .build();
        } catch (ConnectException e) {
            return HttpResponse.builder().statusCode(503).statusReason("Connection Refused").build();
        } catch (UnknownHostException | NoRouteToHostException e) {
            return HttpResponse.builder().statusCode(503).statusReason("Unknown Host").build();
        } catch (InterruptedIOException e) {
            return HttpResponse.builder().statusCode(504).statusReason("Client Timeout").build();
        } catch (IOException e) {
            return HttpResponse.builder().statusCode(503).statusReason("Service Snavailable").build();
        }
    }
}
