package com.hivemq.cli.commands.swarm.commander;

import com.google.gson.Gson;
import com.hivemq.cli.commands.swarm.error.SwarmApiErrorTransformer;
import com.hivemq.cli.openapi.ApiClient;
import com.hivemq.cli.openapi.Configuration;
import com.hivemq.cli.openapi.swarm.CommanderApi;
import com.hivemq.cli.openapi.swarm.RunsApi;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Yannick Weber
 */
public class SwarmStatusCommandIT {

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

    final public @NotNull HiveMQTestContainerExtension hivemq = new HiveMQTestContainerExtension();

    private @NotNull CommandLine commandLine;
    private @NotNull PrintStream out;


    @BeforeEach
    void setUp() throws Exception {

        final CompletableFuture<Void> swarmStartFuture = CompletableFuture.runAsync(swarm::start);
        final CompletableFuture<Void> hivemqStartFuture = CompletableFuture.runAsync(hivemq::start);

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        final ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setHttpClient(okHttpClient);

        final RunsApi runsApi = new RunsApi(apiClient);
        final CommanderApi commanderApi = new CommanderApi(apiClient);
        final Gson gson = new Gson();
        final SwarmApiErrorTransformer errorTransformer = new SwarmApiErrorTransformer(gson);
        commandLine = new CommandLine(new SwarmStatusCommand(gson, () -> runsApi, () -> commanderApi, errorTransformer, out));
        out = mock(PrintStream.class);

        swarmStartFuture.get();
        hivemqStartFuture.get();
    }

    @AfterEach
    void tearDown() {
        swarm.stop();
        hivemq.stop();
    }

    @Test
    void getCommanderStatus() {
        final int execute = commandLine.execute(
                "-url=http://" + swarm.getContainerIpAddress() + ":" + swarm.getMappedPort(REST_PORT)
        );
        assertEquals(0, execute);
        verify(out).println("Status: RUNNING");
    }
}
