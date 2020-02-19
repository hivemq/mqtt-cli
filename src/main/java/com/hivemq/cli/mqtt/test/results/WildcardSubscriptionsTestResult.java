package com.hivemq.cli.mqtt.test.results;

import org.jetbrains.annotations.NotNull;

public class WildcardSubscriptionsTestResult {
    private final boolean success;
    private final TestResult plusWildcardTest;
    private final TestResult hashWildcardTest;

    public @NotNull WildcardSubscriptionsTestResult(final @NotNull TestResult plusWildcardTest,
                                                    final @NotNull TestResult hashWildcardTest) {
        this.plusWildcardTest = plusWildcardTest;
        this.hashWildcardTest = hashWildcardTest;

        success = (plusWildcardTest == TestResult.OK) && (hashWildcardTest == TestResult.OK);
    }

    public boolean isSuccess() { return success; }

    public @NotNull TestResult getPlusWildcardTest() { return plusWildcardTest; }

    public @NotNull TestResult getHashWildcardTest() { return hashWildcardTest; }
}
