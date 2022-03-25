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

package com.hivemq.cli.commands.shell;

import com.google.common.base.Throwables;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.commands.Unsubscribe;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CommandLine.Command(name = "sub", aliases = "subscribe",
        description = "Subscribe this MQTT client to a list of topics")
public class ContextSubscribeCommand extends ShellContextCommand implements Runnable, Subscribe, Unsubscribe {

    private static final int IDLE_TIME = 1000;

    private final @NotNull DefaultCLIProperties defaultCLIProperties;

    @SuppressWarnings("unused") //needed for pico cli - reflection code generation
    public ContextSubscribeCommand() {
        //noinspection ConstantConditions
        this(null, null);
    }

    @Inject
    public ContextSubscribeCommand(
            final @NotNull MqttClientExecutor mqttClientExecutor,
            final @NotNull DefaultCLIProperties defaultCLIProperties) {
        super(mqttClientExecutor);
        this.defaultCLIProperties = defaultCLIProperties;
    }

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via required
    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to subscribe to")
    private @NotNull String @NotNull [] topics;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via default value
    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "2",
            description = "Quality of service for the corresponding topics (default for all: 2)")
    private @NotNull MqttQos @NotNull [] qos;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class,
            description = "A user property of the subscribe message")
    private @Nullable Mqtt5UserProperty @Nullable [] userProperties;

    @CommandLine.Option(names = {"-of", "--outputToFile"},
            description = "A file to which the received publish messages will be written")
    private @Nullable File outputFile;

    @CommandLine.Option(names = {"-oc", "--outputToConsole"}, defaultValue = "false",
            description = "The received messages will be written to the console (default: false)")
    private boolean printToSTDOUT;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-s", "--stay"}, defaultValue = "false",
            description = "The subscribe will block the console and wait for publish messages to print (default: false)")
    private boolean stay;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-b64", "--base64"},
            description = "Specify the encoding of the received messages as Base64 (default: false)")
    private boolean base64;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-J", "--jsonOutput"}, defaultValue = "false",
            description = "Print the received publishes in pretty JSON format", order = 1)
    private boolean jsonOutput;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-T", "--showTopics"}, defaultValue = "false",
            description = "Prepend the specific topic name to the received publish", order = 1)
    private boolean showTopics;

    @Override
    public void run() {
        Logger.trace("Command {} ", this);

        setDefaultOptions();

        logUnusedOptions();

        if (stay) {
            printToSTDOUT = true;
        }

        if (outputFileInvalid(outputFile)) {
            return;
        }

        try {
            qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
            mqttClientExecutor.subscribe(Objects.requireNonNull(contextClient), this);
        } catch (final Exception ex) {
            Logger.error(ex, Throwables.getRootCause(ex).getMessage());
        }

        if (stay) {
            try {
                stay();
            } catch (final InterruptedException ex) {
                Logger.error(ex, Throwables.getRootCause(ex).getMessage());
            }
        }
    }

    private void stay() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        final Runnable waitForDisconnectRunnable = () -> {
            while (Objects.requireNonNull(contextClient).getState().isConnected()) {
                try {
                    Thread.sleep(IDLE_TIME);
                } catch (final InterruptedException e) {
                    return;
                }
            }
            latch.countDown();
        };

        final Runnable waitForExitCommandRunnable = () -> {
            final Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
            latch.countDown();
        };

        final ExecutorService WORKER_THREADS = Executors.newFixedThreadPool(2);

        WORKER_THREADS.submit(waitForDisconnectRunnable);
        WORKER_THREADS.submit(waitForExitCommandRunnable);

        latch.await();

        WORKER_THREADS.shutdownNow();

        if (contextClient != null) {
            if (!contextClient.getState().isConnectedOrReconnect()) {
                removeContext();
            } else {
                mqttClientExecutor.unsubscribe(contextClient, this);
            }
        }
    }

    @Override
    public @NotNull String toString() {
        return getClass().getSimpleName() + "{" + "key=" + ((contextClient == null) ? "null" : getKey()) + ", topics=" +
                Arrays.toString(topics) + ", qos=" + Arrays.toString(qos) + ", outputToConsole=" + printToSTDOUT +
                ", base64=" + base64 + ", jsonOutput=" + jsonOutput + ", showTopics=" + showTopics +
                (userProperties != null ? (", userProperties=" + Arrays.toString(userProperties)) : "") +
                (outputFile != null ? (", publishFile=" + outputFile.getAbsolutePath()) : "") + '}';
    }

    @Override
    public @NotNull String @NotNull [] getTopics() {
        return topics;
    }

    @Override
    public @NotNull MqttQos @NotNull [] getQos() {
        return qos;
    }

    @Override
    public @Nullable File getOutputFile() {
        return outputFile;
    }

    @Override
    public boolean isPrintToSTDOUT() {
        return printToSTDOUT;
    }

    @Override
    public boolean isBase64() {return base64;}

    @Override
    public boolean isJsonOutput() {return jsonOutput;}

    @Override
    public boolean showTopics() {return showTopics;}

    @Override
    public @Nullable Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    private void setDefaultOptions() {
        if (outputFile == null && defaultCLIProperties.getClientSubscribeOutputFile() != null) {
            if (isVerbose()) {
                Logger.trace("Setting value of 'toFile' to {}", defaultCLIProperties.getClientSubscribeOutputFile());
            }
            outputFile = new File(defaultCLIProperties.getClientSubscribeOutputFile());
        }

    }

    private void logUnusedOptions() {
        if (Objects.requireNonNull(contextClient).getConfig().getMqttVersion() == MqttVersion.MQTT_3_1_1) {
            if (userProperties != null) {
                Logger.warn("Subscribe user properties were set but are unused in MQTT version {}",
                        MqttVersion.MQTT_3_1_1);
            }
        }
    }
}
