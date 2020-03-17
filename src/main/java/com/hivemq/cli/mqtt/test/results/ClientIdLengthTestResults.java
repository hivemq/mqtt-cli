package com.hivemq.cli.mqtt.test.results;

import com.hivemq.cli.utils.Tuple;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class ClientIdLengthTestResults {
    private final int maxClientIdLength;
    private List<Tuple<@NotNull Integer, @Nullable String>> testResults;

    public ClientIdLengthTestResults(final int maxClientIdLength, final @NotNull List<Tuple<Integer, String>> testResults) {
        this.maxClientIdLength = maxClientIdLength;
        this.testResults = testResults;
    }

    public int getMaxClientIdLength() { return maxClientIdLength; }

    public @NotNull List<Tuple<@NotNull Integer, @Nullable String>> getTestResults() { return testResults; }
}
