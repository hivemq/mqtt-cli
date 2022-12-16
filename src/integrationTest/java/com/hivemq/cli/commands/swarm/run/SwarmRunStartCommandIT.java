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

package com.hivemq.cli.commands.swarm.run;

import com.google.gson.Gson;
import com.hivemq.cli.commands.swarm.error.SwarmApiErrorTransformer;
import com.hivemq.cli.openapi.ApiClient;
import com.hivemq.cli.openapi.Configuration;
import com.hivemq.cli.openapi.swarm.RunsApi;
import com.hivemq.cli.openapi.swarm.ScenariosApi;
import com.hivemq.cli.utils.TestLoggerUtils;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SwarmRunStartCommandIT {

    private static final @NotNull String IMAGE_NAME = "hivemq/hivemq-swarm";
    private static final int REST_PORT = 8080;

    private final @NotNull Network network = Network.newNetwork();
    private final @NotNull GenericContainer<?> swarm = new GenericContainer<>(IMAGE_NAME).withNetwork(network)
            .withNetworkAliases("swarm")
            .withEnv("SWARM_COMMANDER_AGENTS", "localhost:3881")
            .withEnv("SWARM_COMMANDER_MODE", "rest")
            .withEnv("SWARM_REST_ENABLED", "true")
            .withEnv("SWARM_AGENT_BIND_ADDRESS", "localhost")
            .withEnv("SWARM_AGENT_BIND_PORT", "3881")
            .withEnv("SWARM_REST_LISTENER_HTTP_ENABLED", "true")
            .withEnv("SWARM_REST_LISTENER_HTTP_PORT", Integer.toString(REST_PORT))
            .withEnv("SWARM_REST_LISTENER_HTTP_HOST", "0.0.0.0")
            .waitingFor(Wait.forLogMessage("(.*)Commander REST-API: successfully started.(.*)", 1))
            .withEnv("LOG_LEVEL", "DEBUG")
            .withLogConsumer(outputFrame -> System.out.print("SWARM: " + outputFrame.getUtf8String()))
            .withExposedPorts(REST_PORT);

    private final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension().withNetwork(network).withNetworkAliases("hivemq");

    private @NotNull CommandLine commandLine;
    private @NotNull PrintStream out;

    @BeforeEach
    void setUp() throws Exception {
        TestLoggerUtils.resetLogger();

        final CompletableFuture<Void> swarmStartFuture = CompletableFuture.runAsync(swarm::start);
        final CompletableFuture<Void> hivemqStartFuture = CompletableFuture.runAsync(hivemq::start);

        final OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).build();

        final ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setHttpClient(okHttpClient);

        final RunsApi runsApi = new RunsApi(apiClient);
        final ScenariosApi scenariosApi = new ScenariosApi(apiClient);
        final Gson gson = new Gson();
        final SwarmApiErrorTransformer errorTransformer = new SwarmApiErrorTransformer(gson);
        out = mock(PrintStream.class);
        commandLine =
                new CommandLine(new SwarmRunStartCommand(() -> runsApi, () -> scenariosApi, errorTransformer, out));
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);

        swarmStartFuture.get();
        hivemqStartFuture.get();
    }

    @AfterEach
    void tearDown() {
        swarm.stop();
        hivemq.stop();
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void startScenario() throws Exception {
        final CountDownLatch publishesLatch = new CountDownLatch(10);

        final Mqtt5BlockingClient client = Mqtt5Client.builder().serverPort(hivemq.getMqttPort()).buildBlocking();
        client.connect();
        client.toAsync().subscribeWith().topicFilter("#").callback((ack) -> publishesLatch.countDown()).send().get();

        final String scenario =
                MountableFile.forClasspathResource("SwarmRunStartCommandIT/my-scenario.xml").getResolvedPath();
        final int execute = commandLine.execute("-url=http://" + swarm.getHost() + ":" + swarm.getMappedPort(REST_PORT),
                "-f=" + scenario);
        assertEquals(0, execute);

        publishesLatch.await();
        verify(out, times(1)).println("Uploading scenario from file '" + scenario + "'.");
        verify(out, times(1)).println("Successfully uploaded scenario. Scenario-id: " + 1);
        verify(out, times(1)).println("Run id: 1");
        verify(out, times(1)).println("Run status: STARTING");
    }
}
