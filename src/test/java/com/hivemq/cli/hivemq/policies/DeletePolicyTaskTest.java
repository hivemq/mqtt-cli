package com.hivemq.cli.hivemq.policies;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DeletePolicyTaskTest {

    private final @NotNull PoliciesApi policiesApi = mock(PoliciesApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);

    private static final @NotNull String POLICY_ID = "policy-1";

    @Test
    void execute_validId_success() {
        final DeletePolicyTask task = new DeletePolicyTask(outputFormatter, policiesApi, POLICY_ID);
        assertTrue(task.execute());
    }

    @Test
    void execute_apiException_printError() throws ApiException {
        final DeletePolicyTask task = new DeletePolicyTask(outputFormatter, policiesApi, POLICY_ID);
        doThrow(ApiException.class).when(policiesApi).deletePolicy(any());
        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printApiException(any(), any());
    }
}
