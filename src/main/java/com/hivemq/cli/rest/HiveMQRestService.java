/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli.rest;

import com.hivemq.cli.openapi.ApiCallback;
import com.hivemq.cli.openapi.ApiClient;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.Configuration;
import com.hivemq.cli.openapi.hivemq.ClientItem;
import com.hivemq.cli.openapi.hivemq.ClientList;
import com.hivemq.cli.openapi.hivemq.MqttClientsApi;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class HiveMQRestService {

    private final @NotNull ApiClient apiClient;
    private final @NotNull MqttClientsApi clientsApi;

    private static final long CONNECT_TIMEOUT = 60;

    public HiveMQRestService(final @NotNull String host, final double requestPerSecondLimit) {
        final OkHttpClient okHttpClient = buildOkHttpClient(requestPerSecondLimit);

        apiClient = Configuration.getDefaultApiClient();
        apiClient.setHttpClient(okHttpClient);
        apiClient.setBasePath(host);

        clientsApi = new MqttClientsApi(apiClient);
    }


    public ClientList getClientIds(final @Nullable String cursor) throws ApiException {
        return clientsApi.getAllMqttClients(2500, cursor);
    }

    public Call getClientDetails(final @NotNull String clientId,
                                 final @NotNull ApiCallback<ClientItem> callback) throws ApiException {

        return clientsApi.getMqttClientDetailsAsync(clientId, callback);
    }


    public @NotNull ApiClient getApiClient() { return apiClient; }

    public @NotNull  MqttClientsApi getClientsApi() { return clientsApi; }

    private @NotNull OkHttpClient buildOkHttpClient(final double requestsPerSecondLimit) {
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new RateLimitInterceptor(requestsPerSecondLimit))
                .build();
    }
}
