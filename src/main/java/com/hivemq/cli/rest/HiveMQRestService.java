/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.cli.rest;

import com.hivemq.cli.openapi.ApiClient;
import com.hivemq.cli.openapi.Configuration;
import com.hivemq.cli.openapi.hivemq.MqttClientsApi;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class HiveMQRestService {

    private static final long CONNECT_TIMEOUT = 60;

    public static @NotNull MqttClientsApi getMqttClientsApi(
            final @NotNull String host, final double requestPerSecondLimit) {
        final ApiClient apiClient = buildApiClient(host, requestPerSecondLimit);
        return new MqttClientsApi(apiClient);
    }

    public static @NotNull PoliciesApi getPoliciesApi(final @NotNull String host, final double requestPerSecondLimit) {

        final ApiClient apiClient = buildApiClient(host, requestPerSecondLimit);
        return new PoliciesApi(apiClient);
    }

    public static @NotNull SchemasApi getSchemasApi(final @NotNull String host, final double requestPerSecondLimit) {

        final ApiClient apiClient = buildApiClient(host, requestPerSecondLimit);
        return new SchemasApi(apiClient);
    }

    private static @NotNull ApiClient buildApiClient(final @NotNull String host, final double requestsPerSecondLimit) {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new RateLimitInterceptor(requestsPerSecondLimit))
                .build();
        final ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setHttpClient(okHttpClient);
        apiClient.setBasePath(host);
        return apiClient;
    }
}
