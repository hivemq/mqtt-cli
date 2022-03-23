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

import com.hivemq.cli.utils.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PayloadTestResults {

    private final int payloadSize;
    private final @NotNull List<Tuple<Integer, TestResult>> testResults;

    public PayloadTestResults(final int payloadSize, final @NotNull List<Tuple<Integer, TestResult>> testResults) {
        this.payloadSize = payloadSize;
        this.testResults = testResults;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public @NotNull List<Tuple<Integer, TestResult>> getTestResults() {
        return testResults;
    }
}
