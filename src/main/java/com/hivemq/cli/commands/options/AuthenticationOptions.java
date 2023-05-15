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

package com.hivemq.cli.commands.options;

import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.converters.ByteBufferConverter;
import com.hivemq.cli.converters.EnvVarToByteBufferConverter;
import com.hivemq.cli.converters.PasswordFileToByteBufferConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.nio.ByteBuffer;
import java.util.Objects;

public class AuthenticationOptions {

    @CommandLine.Option(names = {"-u", "--user"}, description = "The username for authentication")
    private @Nullable String user;

    @CommandLine.Option(names = {"-pw", "--password"},
                        arity = "0..1",
                        interactive = true,
                        converter = ByteBufferConverter.class,
                        description = "The password for authentication")
    private @Nullable ByteBuffer password;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-pw:env"},
                        arity = "0..1",
                        converter = EnvVarToByteBufferConverter.class,
                        fallbackValue = "MQTT_CLI_PW",
                        description = "The password for authentication read in from an environment variable")
    private void setPasswordFromEnv(final @NotNull ByteBuffer passwordEnvironmentVariable) {
        password = passwordEnvironmentVariable;
    }

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-pw:file"},
                        converter = PasswordFileToByteBufferConverter.class,
                        description = "The password for authentication read in from a file")
    private void setPasswordFromFile(final @NotNull ByteBuffer passwordFromFile) {
        password = passwordFromFile;
    }

    public @Nullable String getUser() {
        return user;
    }

    public @Nullable ByteBuffer getPassword() {
        return password;
    }

    @Override
    public @NotNull String toString() {
        return "AuthenticationOptions{" + "user='" + user + '\'' + ", password=" + password + '}';
    }

    public void setDefaultOptions() {
        final DefaultCLIProperties properties = Objects.requireNonNull(MqttCLIMain.MQTT_CLI).defaultCLIProperties();
        if (user == null) {
            user = properties.getUsername();
        }
        if (password == null) {
            try {
                password = properties.getPassword();
            } catch (final Exception e) {
                Logger.error("Could not read password from properties", password);
            }
        }
    }
}
