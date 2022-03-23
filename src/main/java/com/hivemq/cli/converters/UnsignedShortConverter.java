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

package com.hivemq.cli.converters;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class UnsignedShortConverter implements CommandLine.ITypeConverter<Integer> {

    static final @NotNull String WRONG_INPUT_MESSAGE = "Value must be in range [0 - 65_535]";

    private final static int MAX_VALUE = 65_535;

    @Override
    public @NotNull Integer convert(final @NotNull String s) throws Exception {
        try {
            final int interval = Integer.parseInt(s);
            if (!(interval >= 0 && interval <= MAX_VALUE)) {
                throw new Exception(WRONG_INPUT_MESSAGE);
            }
            return interval;
        } catch (final NumberFormatException p) {
            throw new Exception(WRONG_INPUT_MESSAGE);
        }
    }
}
