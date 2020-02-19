package com.hivemq.cli.mqtt.test.results;

import com.hivemq.cli.utils.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TopicLengthTestResults {
    private final int maxTopicLength;
    private final List<Tuple<Integer, TestResult>> testResults;

    public TopicLengthTestResults(int maxTopicLength, List<Tuple<Integer, TestResult>> testResults) {
        this.maxTopicLength = maxTopicLength;
        this.testResults = testResults;
    }

    public int getMaxTopicLength() { return maxTopicLength; }

    public @NotNull List<Tuple<Integer, TestResult>> getTestResults() { return testResults; }

}
