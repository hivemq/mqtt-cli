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

import java.util.LinkedList;
import java.util.List;

public class AsciiCharsInClientIdTestResults {

    private final @NotNull List<Tuple<Character, String>> testResults;

    public AsciiCharsInClientIdTestResults(final @NotNull List<Tuple<Character, String>> testResults) {
        this.testResults = testResults;
    }

    public @NotNull List<Tuple<Character, String>> getTestResults() {
        return testResults;
    }

    public @NotNull List<Character> getUnsupportedChars() {
        final List<Character> unsupportedChars = new LinkedList<>();
        for (final Tuple<Character, String> tuple : testResults) {
            if (tuple.getValue() == null || !("SUCCESS").equals(tuple.getValue())) {
                unsupportedChars.add(tuple.getKey());
            }
        }
        return unsupportedChars;
    }
}
