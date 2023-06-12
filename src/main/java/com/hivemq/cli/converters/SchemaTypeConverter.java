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

public class SchemaTypeConverter implements CommandLine.ITypeConverter<String> {

    static final @NotNull String WRONG_INPUT_MESSAGE = "Value must be json or protobuf";

    @Override
    public @NotNull String convert(final @NotNull String s) throws Exception {
        switch (s.toLowerCase()) {
            case "json":
                return "JSON";
            case "protobuf":
                return "PROTOBUF";
            default:
                throw new Exception(WRONG_INPUT_MESSAGE);
        }
    }
}
