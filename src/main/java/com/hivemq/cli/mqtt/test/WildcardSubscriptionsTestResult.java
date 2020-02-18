package com.hivemq.cli.mqtt.test;

import org.jetbrains.annotations.NotNull;

public class WildcardSubscriptionsTestResult {
    private final boolean success;
    private final WildcardTestResult plusWildcardTest;
    private final WildcardTestResult hashWildcardTest;

    public @NotNull WildcardSubscriptionsTestResult(final @NotNull WildcardTestResult plusWildcardTest,
                                                    final @NotNull WildcardTestResult hashWildcardTest) {
        this.plusWildcardTest = plusWildcardTest;
        this.hashWildcardTest = hashWildcardTest;

        success = (plusWildcardTest == WildcardTestResult.OK) && (hashWildcardTest == WildcardTestResult.OK);
    }

    public boolean isSuccess() { return success; }

    public @NotNull  WildcardTestResult getPlusWildcardTest() { return plusWildcardTest; }

    public @NotNull WildcardTestResult getHashWildcardTest() { return hashWildcardTest; }
}
