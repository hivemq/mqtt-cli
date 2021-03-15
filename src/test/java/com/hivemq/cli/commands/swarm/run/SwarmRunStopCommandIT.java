package com.hivemq.cli.commands.swarm.run;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.hivemq.cli.commands.swarm.error.SwarmApiErrorTransformer;
import com.hivemq.cli.openapi.ApiClient;
import com.hivemq.cli.openapi.Configuration;
import com.hivemq.cli.openapi.swarm.*;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;
import picocli.CommandLine;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Yannick Weber
 */
public class SwarmRunStopCommandIT {

    public static final @NotNull String IMAGE_NAME = "hivemq/hivemq-swarm";
    public static final int REST_PORT = 8080;

    private final @NotNull Network network = Network.newNetwork();

    private final @NotNull GenericContainer<?> swarm = new GenericContainer<>(IMAGE_NAME)
            .withNetwork(network)
            .withNetworkAliases("swarm")
            .withEnv("SWARM_COMMANDER_AGENTS", "localhost:3881")
            .withEnv("SWARM_COMMANDER_MODE", "rest")
            .withEnv("SWARM_REST_ENABLED", "true")
            .withEnv("SWARM_REST_LISTENER_HTTP_ENABLED", "true")
            .withEnv("SWARM_REST_LISTENER_HTTP_PORT", Integer.toString(REST_PORT))
            .withEnv("SWARM_REST_LISTENER_HTTP_HOST", "0.0.0.0")
            .waitingFor(Wait.forLogMessage("(.*)Commander REST-API: successfully started.(.*)", 1))
            .withEnv("LOG_LEVEL", "DEBUG")
            .withLogConsumer(outputFrame -> System.out.print("SWARM: " + outputFrame.getUtf8String()))
            .withExposedPorts(REST_PORT);

    final public @NotNull HiveMQTestContainerExtension hivemq = new HiveMQTestContainerExtension()
            .withNetwork(network)
            .withNetworkAliases("hivemq");

    private @NotNull CommandLine commandLine;
    private @NotNull RunsApi runsApi;
    private @NotNull CommanderApi commanderApi;
    private @NotNull String scenarioBase64;
    private @NotNull ScenariosApi scenariosApi;

    @BeforeEach
    void setUp() throws Exception {

        final CompletableFuture<Void> swarmStartFuture = CompletableFuture.runAsync(swarm::start);
        final CompletableFuture<Void> hivemqStartFuture = CompletableFuture.runAsync(hivemq::start);
        swarmStartFuture.get();
        hivemqStartFuture.get();

        final byte[] bytes = Files.toByteArray(new File("src/test/resources/SwarmRunStopCommandIT/blockScenario.xml"));
        final String scenarioString =
                new String(bytes, StandardCharsets.UTF_8).replace("localhost", "broker");
        scenarioBase64 = Base64.getEncoder().encodeToString(scenarioString.getBytes(StandardCharsets.UTF_8));

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        final ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath("http://localhost:" + swarm.getMappedPort(8080));
        apiClient.setHttpClient(okHttpClient);

        runsApi = new RunsApi(apiClient);
        final Gson gson = new Gson();
        final SwarmApiErrorTransformer errorTransformer = new SwarmApiErrorTransformer(gson);
        commandLine = new CommandLine(new SwarmRunStopCommand(() -> runsApi, errorTransformer));

        commanderApi = new CommanderApi(apiClient);
        scenariosApi = new ScenariosApi(apiClient);
    }

    @AfterEach
    void tearDown() {
        swarm.stop();
        hivemq.stop();
    }

    @Test
    void stopRun() throws Exception {


        final Mqtt5BlockingClient client = Mqtt5Client.builder().serverPort(hivemq.getMqttPort()).buildBlocking();
        client.connect();
        final Mqtt5BlockingClient.Mqtt5Publishes publishes = client.publishes(MqttGlobalPublishFilter.ALL);
        client.toAsync().subscribeWith().topicFilter("#").send();

        final UploadScenarioRequest uploadScenarioRequest = new UploadScenarioRequest()
                .scenarioType("XML").scenario(scenarioBase64).scenarioName("my-scenario");
        scenariosApi.uploadScenario(uploadScenarioRequest);

        final StartRunRequest startRunRequest = new StartRunRequest();
        startRunRequest.setScenarioId("2");
        runsApi.startRun(startRunRequest);

        // the scenario is started
        publishes.receive();


        // stop the scenario
        final int execute = commandLine.execute(
                "-url=http://" + swarm.getContainerIpAddress() + ":" + swarm.getMappedPort(REST_PORT),
                "-r" + 1
        );
        assertEquals(0, execute);

        await().atMost(Duration.ofSeconds(3))
                .until(() -> {
                    final CommanderStateResponse commanderStatus = commanderApi.getCommanderStatus();
                    System.out.println(commanderStatus);
                    return "READY".equals(commanderStatus.getCommanderStatus());
                });

        final RunResponse run = runsApi.getRun("1");
        assertEquals("STOPPED", run.getRunStatus());

        // stop a run that does not exist
        final int execute2 = commandLine.execute(
                "-url=http://" + swarm.getContainerIpAddress() + ":" + swarm.getMappedPort(REST_PORT),
                "-r" + 1
        );
        assertEquals(-1, execute2);
    }
}
