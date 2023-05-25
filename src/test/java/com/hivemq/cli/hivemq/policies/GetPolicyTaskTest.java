package com.hivemq.cli.hivemq.policies;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.openapi.hivemq.Policy;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetPolicyTaskTest {

    private final @NotNull PoliciesApi policiesApi = mock(PoliciesApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);

    private static final @NotNull String POLICY_ID = "policy-1";

    @Test
    void execute_validId_success() throws ApiException {
        final Policy policy = new Policy();

        final GetPolicyTask task = new GetPolicyTask(outputFormatter, policiesApi, POLICY_ID);

        when(policiesApi.getPolicy(eq(POLICY_ID), isNull())).thenReturn(policy);

        assertTrue(task.execute());
        verify(policiesApi, times(1)).getPolicy(eq(POLICY_ID), isNull());
        verify(outputFormatter, times(1)).printJson(policy);
    }

    @Test
    void execute_apiException_printError() throws ApiException {
        final GetPolicyTask task = new GetPolicyTask(outputFormatter, policiesApi, POLICY_ID);

        when(policiesApi.getPolicy(any(), isNull())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printApiException(any(), any());
    }

}
