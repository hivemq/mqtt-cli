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

package com.hivemq.cli;

import com.hivemq.cli.ioc.DaggerMqttCLI;
import com.hivemq.cli.ioc.MqttCLI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.security.Security;

public class MqttCLIMain {

    public static @Nullable MqttCLI MQTTCLI = null;

    public static void main(final @NotNull String... args) {

        Security.setProperty("crypto.policy", "unlimited");

        MQTTCLI = DaggerMqttCLI.create();
        final CommandLine commandLine = MQTTCLI.cli();
        final DefaultCLIProperties defaultCLIProperties = MQTTCLI.defaultCLIProperties();

        try {
            defaultCLIProperties.init();
        } catch (final Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (args.length == 0) {
            System.out.println(commandLine.getUsageMessage());
            System.exit(0);
        }

        final int exitCode = commandLine.execute(args);

        System.exit(exitCode);

    }

    public static class CLIVersionProvider implements CommandLine.IVersionProvider {

        @Override
        public @NotNull String @NotNull [] getVersion() {
            String version = getClass().getPackage().getImplementationVersion();
            if (version == null) {
                version = "DEVELOPMENT";
            }
            return new String[]{
                    version,
                    "Picocli " + CommandLine.VERSION,
                    "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                    "OS: ${os.name} ${os.version} ${os.arch}"};
        }
    }
}
