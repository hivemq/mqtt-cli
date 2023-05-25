package com.hivemq.cli.commands.hivemq.policies;

import com.google.gson.Gson;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.rest.HiveMQRestService;
import com.hivemq.cli.utils.TestLoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreatePolicyCommandTest {

    private final @NotNull HiveMQRestService hiveMQRestService = mock(HiveMQRestService.class);
    private final @NotNull Gson gson = new Gson();
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);
    private final @NotNull PoliciesApi policiesApi = mock(PoliciesApi.class);

    private final @NotNull CommandLine commandLine =
            new CommandLine(new CreatePolicyCommand(hiveMQRestService, outputFormatter, gson));

    private static final @NotNull String POLICY_JSON =
            "{ \"id\": \"policy-1\", \"matching\": { \"topicFilter\": \"a/#\" } }";

    @BeforeEach
    void setUp() {
        TestLoggerUtils.resetLogger();
        when(hiveMQRestService.getPoliciesApi(any(), anyDouble())).thenReturn(policiesApi);
    }

    @Test
    void call_definitionMissing_error() {
        assertEquals(2, commandLine.execute());
    }

    @Test
    void call_argumentDefinitionEmpty_error() {
        assertEquals(1, commandLine.execute("--definition="));
        verify(outputFormatter).printError(eq("The policy definition must not be empty."));
    }

    @Test
    void call_fileDefinitionEmpty_error() throws IOException {
        final File policyFile = File.createTempFile("policy", ".json");
        Files.write(policyFile.toPath(), "".getBytes());
        assertEquals(1, commandLine.execute("--file=" + policyFile.getAbsolutePath()));
        verify(outputFormatter).printError(eq("The policy definition must not be empty."));
    }

    @Test
    void call_bothFileAndArgumentDefinition_error() throws IOException {
        final File policyFile = File.createTempFile("policy", ".json");
        assertEquals(2, commandLine.execute("--definition='abc'", "--file=" + policyFile.getAbsolutePath()));
    }

    @Test
    void call_urlAndRateLimitPassed_usedInApi() {
        assertEquals(0, commandLine.execute("--rate=123", "--url=test-url", "--definition=" + POLICY_JSON));
        verify(hiveMQRestService).getPoliciesApi(eq("test-url"), eq(123d));
    }

    @Test
    void call_taskSuccessful_return0() {
        assertEquals(0, commandLine.execute("--definition=" + POLICY_JSON));
    }

    @Test
    void call_taskFailed_return1() throws ApiException {
        when(policiesApi.createPolicy(any())).thenThrow(ApiException.class);
        assertEquals(1, commandLine.execute("--definition=" + POLICY_JSON));
    }
}
