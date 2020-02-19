package com.hivemq.cli.mqtt.test.results;


import com.hivemq.cli.utils.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PayloadTestResults {
    private final int payloadSize;
    private final List<Tuple<Integer, TestResult>> testResults;

    public PayloadTestResults(final int payloadSize, final @NotNull List<Tuple<Integer, TestResult>> testResults) {
        this.payloadSize = payloadSize;
        this.testResults = testResults;
    }

    public int getPayloadSize() { return payloadSize; }

    public @NotNull List<Tuple<Integer, TestResult>> getTestResults() { return testResults; }
}
