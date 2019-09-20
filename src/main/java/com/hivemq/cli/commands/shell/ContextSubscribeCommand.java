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
package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.commands.Unsubscribe;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.cli.utils.PropertiesUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CommandLine.Command(name = "sub",
        aliases = "subscribe",
        description = "Subscribe this mqtt client to a list of topics")
public class ContextSubscribeCommand extends ShellContextCommand implements Runnable, Subscribe, Unsubscribe {

    public static final int IDLE_TIME = 1000;

    //needed for pico cli - reflection code generation
    public ContextSubscribeCommand() {
        this(null);
    }

    @Inject
    public ContextSubscribeCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }


    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to subscribe to")
    @NotNull
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "0", description = "Quality of service for the corresponding topics (default for all: 0)")
    @NotNull
    private MqttQos[] qos;

    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class, description = "A user property for the subscribe message")
    @Nullable Mqtt5UserProperty[] userProperties;

    @CommandLine.Option(names = {"-of", "--outputToFile"}, description = "A file to which the received publish messages will be written")
    @Nullable
    private File receivedMessagesFile;

    @CommandLine.Option(names = {"-oc", "--outputToConsole"}, defaultValue = "false", description = "The received messages will be written to the console (default: false)")
    private boolean printToSTDOUT;

    @CommandLine.Option(names = {"-s", "--stay"}, defaultValue = "false", description = "The subscribe will block the console and wait for publish messages to print (default: false)")
    private boolean stay;

    @CommandLine.Option(names = {"-b64", "--base64"}, description = "Specify the encoding of the received messages as Base64 (default: false)")
    private boolean base64;

    @Override
    public void run() {


        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        setDefaultOptions();

        logUnusedOptions();

        if (stay) {
            printToSTDOUT = true;
        }


        try {
            qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
            mqttClientExecutor.subscribe(contextClient, this);
        } catch (final Exception ex) {
            if (isDebug()) {
                Logger.error(ex);
            }
            Logger.error(ex.getMessage());
        }

        if (stay) {
            try {
                stay();
            } catch (final InterruptedException e) {
                if (isDebug()) {
                    Logger.debug(e);
                }
                Logger.error(e.getMessage());
            }
        }
    }

    private void stay() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(1);

        final Runnable waitForDisconnectRunnable = new Runnable() {

            @Override
            public void run() {
                while (contextClient.getState().isConnected()) {
                    try {
                        Thread.sleep(IDLE_TIME);
                    }
                    catch (final InterruptedException e) {
                        return;
                    }
                }
                latch.countDown();
            }
        };

        final Runnable waitForExitCommandRunnable = new Runnable() {
            @Override
            public void run() {
                final Scanner scanner = new Scanner(System.in);
                scanner.nextLine();
                latch.countDown();
                return;
            }
        };

        final ExecutorService WORKER_THREADS = Executors.newFixedThreadPool(2);

        WORKER_THREADS.submit(waitForDisconnectRunnable);
        WORKER_THREADS.submit(waitForExitCommandRunnable);

        latch.await();

        WORKER_THREADS.shutdownNow();

        if (!contextClient.getState().isConnectedOrReconnect()) {
            Logger.info("Client disconnected.");
            removeContext();
        }
        else {
            mqttClientExecutor.unsubscribe(contextClient, this);
        }

    }

    @Override
    public String toString() {
        return "ContextSubscribe:: {" +
                "key=" + getKey() +
                ", topics=" + Arrays.toString(topics) +
                ", qos=" + Arrays.toString(qos) +
                ", userProperties=" + userProperties +
                ", toFile=" + receivedMessagesFile +
                ", outputToConsole=" + printToSTDOUT +
                ", base64=" + base64 +
                '}';
    }

    public void setDefaultOptions() {

        if (receivedMessagesFile == null && PropertiesUtils.DEFAULT_SUBSCRIBE_OUTPUT_FILE != null) {
            if (isVerbose()) {
                Logger.trace("Setting value of 'toFile' to {}", PropertiesUtils.DEFAULT_SUBSCRIBE_OUTPUT_FILE);
            }
            receivedMessagesFile = new File(PropertiesUtils.DEFAULT_SUBSCRIBE_OUTPUT_FILE);
        }

    }

    private void logUnusedOptions() {
        if (contextClient.getConfig().getMqttVersion() == MqttVersion.MQTT_3_1_1) {
            if (userProperties != null) {
                Logger.warn("Subscribe user properties were set but are unused in Mqtt version {}", MqttVersion.MQTT_3_1_1);
            }
        }
    }

    @NotNull
    public String[] getTopics() {
        return topics;
    }

    public void setTopics(final String[] topics) {
        this.topics = topics;
    }

    @NotNull
    public MqttQos[] getQos() {
        return qos;
    }

    public void setQos(final MqttQos[] qos) {
        this.qos = qos;
    }

    @Nullable
    public File getReceivedMessagesFile() {
        return receivedMessagesFile;
    }

    public void setReceivedMessagesFile(@Nullable final File receivedMessagesFile) {
        this.receivedMessagesFile = receivedMessagesFile;
    }

    public boolean isPrintToSTDOUT() {
        return printToSTDOUT;
    }

    public void setPrintToSTDOUT(final boolean printToSTDOUT) {
        this.printToSTDOUT = printToSTDOUT;
    }

    public boolean isBase64() {
        return base64;
    }

    public void setBase64(final boolean base64) {
        this.base64 = base64;
    }

    @Override
    @Nullable
    public Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    public void setUserProperties(@Nullable final Mqtt5UserProperty... userProperties) {
        this.userProperties = userProperties;
    }
}
