package com.hivemq.cli.mqtt.test.results;

import com.hivemq.cli.utils.Tuple;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class AsciiCharsInClientIdTestResults {
    private final List<Tuple<@NotNull Character, @Nullable Mqtt3ConnAckReturnCode>> testResults;

    public AsciiCharsInClientIdTestResults(final @NotNull List<Tuple<Character, Mqtt3ConnAckReturnCode>> testResults) {
        this.testResults = testResults;
    }

    public @NotNull List<Tuple<Character, Mqtt3ConnAckReturnCode>> getTestResults() {
        return testResults;
    }

    public @NotNull List<Character> getUnsupportedChars() {
        final List<Character> unsupportedChars = new LinkedList<>();
        for (Tuple<Character, Mqtt3ConnAckReturnCode> tuple: testResults) {
            if (tuple.getValue() != Mqtt3ConnAckReturnCode.SUCCESS) {
                unsupportedChars.add(tuple.getKey());
            }
        }
        return unsupportedChars;
    }

}
