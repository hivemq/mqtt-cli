/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.cli.mqtt.test.results;

import org.jetbrains.annotations.NotNull;

public class WildcardSubscriptionsTestResult {

    private final boolean success;
    private final @NotNull TestResult plusWildcardTest;
    private final @NotNull TestResult hashWildcardTest;

    public WildcardSubscriptionsTestResult(
            final @NotNull TestResult plusWildcardTest, final @NotNull TestResult hashWildcardTest) {
        this.plusWildcardTest = plusWildcardTest;
        this.hashWildcardTest = hashWildcardTest;
        success = (plusWildcardTest == TestResult.OK) && (hashWildcardTest == TestResult.OK);
    }

    public boolean isSuccess() {
        return success;
    }

    public @NotNull TestResult getPlusWildcardTest() {
        return plusWildcardTest;
    }

    public @NotNull TestResult getHashWildcardTest() {
        return hashWildcardTest;
    }
}
