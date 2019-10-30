/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli;

import com.hivemq.cli.ioc.DaggerMqttCLI;
import com.hivemq.cli.mqtt.ClientData;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.PropertiesUtils;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.writers.ConsoleWriter;
import picocli.CommandLine;

import java.security.Security;
import java.util.*;
import java.util.concurrent.CompletableFuture;


public class MqttCLIMain {

    public static void main(final String[] args) {

        Security.setProperty("crypto.policy", "unlimited");

        final CommandLine commandLine = DaggerMqttCLI.create().commandLine();

        Configurator.defaultConfig()
                .writer(new ConsoleWriter())
                .formatPattern("{context:identifier}: {message}")
                .level(Level.INFO)
                .activate();

        if (args.length == 0) {
            System.out.println(commandLine.getUsageMessage());
            System.exit(0);
        }

        try {
            setupProperties();
        }
        catch (final Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new DisconnectAllClientsTask());

        final int exitCode = commandLine.execute(args);

        System.exit(exitCode);

    }

    private static void setupProperties() throws Exception {
        Properties properties = null;

        properties = PropertiesUtils.readDefaultProperties(PropertiesUtils.PROPERTIES_FILE_PATH);

        if (properties == null) {
            PropertiesUtils.createDefaultPropertiesFile(PropertiesUtils.DEFAULT_PROPERTIES, PropertiesUtils.PROPERTIES_FILE_PATH);
        }
        else {
            PropertiesUtils.setDefaultProperties(properties);
        }
    }

    private static class DisconnectAllClientsTask extends Thread {

        @Override
        public void run() {

            final Map<String, ClientData> clientKeyToClientData = MqttClientExecutor.getClientDataMap();

            final List<CompletableFuture<Void>> disconnectFutures = new ArrayList<CompletableFuture<Void>>();

            for (final Map.Entry<String, ClientData> entry: clientKeyToClientData.entrySet()) {

                final MqttClient client = entry.getValue().getClient();
                if (client.getConfig().getState().isConnectedOrReconnect()) {
                    switch (client.getConfig().getMqttVersion()) {
                        case MQTT_5_0:
                            disconnectFutures.add(((Mqtt5Client) client).toAsync().disconnect());
                            break;
                        case MQTT_3_1_1:
                            disconnectFutures.add(((Mqtt3Client) client).toAsync().disconnect());
                            break;
                    }
                }
            }

            CompletableFuture.allOf(disconnectFutures.toArray(new CompletableFuture<?>[0]))
                    .join();
        }
    }

    public static class CLIVersionProvider implements CommandLine.IVersionProvider {

        @Override
        public String[] getVersion() throws Exception {
            String version = getClass().getPackage().getImplementationVersion();
            if (version == null) {
                version = "DEVELOPMENT";
            }
            return new String[]{version,
                    "Picocli " + CommandLine.VERSION,
                    "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                    "OS: ${os.name} ${os.version} ${os.arch}"};
        }
    }
}
