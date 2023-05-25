package com.hivemq.cli.utils;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.commands.hivemq.policies.DeletePolicyCommand;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class DataGovernanceUtils {

    private static final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);
    private static final @NotNull HiveMQRestService hiveMQRestService = new HiveMQRestService();

//    private void deleteAllPoliciesAndSchemas() {
//        final CommandLine deleteSchema = new CommandLine(new DeleteSchemaCommand(hiveMQRestService, outputFormatter));
//        final CommandLine deletePolicy = new CommandLine(new DeletePolicyCommand(hiveMQRestService, outputFormatter));
//        final CommandLine listPolicies = new CommandLine(new ListPolicyCommand(hiveMQRestService, outputFormatter));
//    }
//
//    private void deleteAllPolicies() {
//        final CommandLine deletePolicy = new CommandLine(new DeletePolicyCommand(hiveMQRestService, outputFormatter));
//    }

    public static void deletePolicy(final @NotNull String restApiUrl, final @NotNull String policyId) {
        TestLoggerUtils.resetLogger();
        final CommandLine command = new CommandLine(new DeletePolicyCommand(hiveMQRestService, outputFormatter));
        command.execute("--url=" + restApiUrl, "--id=" + policyId);
    }

    public static void assertJsonEquals(final @NotNull String json1, final @NotNull String json2) {
        try {
            assertEquals(JsonParser.parseString(json1), JsonParser.parseString(json2));
        } catch (final JsonSyntaxException exception) {
            assertEquals(json1, json2);
        }
    }
}
