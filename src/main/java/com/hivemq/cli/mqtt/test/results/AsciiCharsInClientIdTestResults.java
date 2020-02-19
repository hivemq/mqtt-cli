package com.hivemq.cli.mqtt.test.results;

import com.hivemq.cli.utils.Tuple;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AsciiCharsInClientIdTestResults {
    private final List<Tuple<Character, Mqtt3ConnAckReturnCode>> testResults;

    public AsciiCharsInClientIdTestResults(final @NotNull List<Tuple<Character, Mqtt3ConnAckReturnCode>> testResults) {
        this.testResults = testResults;
    }

    public @NotNull List<Tuple<Character, Mqtt3ConnAckReturnCode>> getTestResults() {
        return testResults;
    }
}
