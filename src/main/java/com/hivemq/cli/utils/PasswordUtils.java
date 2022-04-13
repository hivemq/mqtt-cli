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

package com.hivemq.cli.utils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

public class PasswordUtils {

    public static char @NotNull [] readPassword(final @NotNull String @NotNull ... args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException();
        }
        String promptMessage = "";
        if (args.length == 1) {
            promptMessage = args[0];
        }

        final Console console = System.console();
        if (console != null) {
            return console.readPassword("%s", promptMessage);
        } else { // Safe password prompt is not possible - maybe called program from IDE?
            System.out.print(promptMessage);
            final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            final String result = in.readLine();
            return result.toCharArray();
        }
    }
}
