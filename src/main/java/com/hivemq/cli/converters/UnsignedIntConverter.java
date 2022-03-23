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

public class UnsignedIntConverter implements CommandLine.ITypeConverter<Long> {

    static final @NotNull String WRONG_INPUT_MESSAGE = "Value must be in range [0 - 4_294_967_295]";

    private final static long MAX_VALUE = 4_294_967_295L;

    @Override
    public @NotNull Long convert(final @NotNull String s) throws Exception {
        try {
            final long interval = Long.parseLong(s);
            if (!(interval >= 0 && interval <= MAX_VALUE)) {
                throw new Exception(WRONG_INPUT_MESSAGE);
            }
            return interval;
        } catch (final NumberFormatException p) {
            throw new Exception(WRONG_INPUT_MESSAGE);
        }
    }
}
