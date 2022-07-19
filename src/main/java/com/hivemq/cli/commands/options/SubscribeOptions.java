package com.hivemq.cli.commands.options;

import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class SubscribeOptions {

    public SubscribeOptions() {
        setDefaultOptions();
    }

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

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-of", "--outputToFile"},
            description = "A file to which the received publish messages will be written")
    private @Nullable File outputFile;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-oc", "--outputToConsole"}, hidden = true, defaultValue = "true",
            description = "The received messages will be written to the console (default: true)")
    private boolean printToSTDOUT;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-b64", "--base64"},
            description = "Specify the encoding of the received messages as Base64 (default: false)")
    private boolean base64;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-J", "--jsonOutput"}, defaultValue = "false",
            description = "Print the received publishes in pretty JSON format")
    private boolean jsonOutput;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-T", "--showTopics"}, defaultValue = "false",
            description = "Prepend the specific topic name to the received publish")
    private boolean showTopics;

    public @NotNull String @NotNull [] getTopics() {
        return topics;
    }

    public @NotNull MqttQos @NotNull [] getQos() {
        return qos;
    }

    public @Nullable File getOutputFile() {
        return outputFile;
    }

    public boolean isPrintToSTDOUT() {
        return printToSTDOUT;
    }

    public boolean isBase64() {return base64;}

    public boolean isJsonOutput() {return jsonOutput;}

    public @Nullable Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    public @Nullable Mqtt5UserProperty[] getUserPropertiesRaw() {
        return userProperties;
    }

    public boolean isShowTopics() {
        return showTopics;
    }

    public void setPrintToSTDOUT(final boolean printToSTDOUT) {
        this.printToSTDOUT = printToSTDOUT;
    }

    public boolean isOutputFileInvalid(final @Nullable File outputFile) {
        if (outputFile == null) {
            // option --outputToFile was not used
            return false;
        }
        if (outputFile.isDirectory()) {
            Logger.error("Cannot create output file {} as it is a directory", outputFile.getAbsolutePath());
            return true;
        }

        try {
            if (!outputFile.createNewFile()) { // This is only false if the file already exists
                Logger.debug("Writing to existing output file {}", outputFile.getAbsolutePath());
            }
        } catch (final @NotNull IOException e) {
            Logger.error("Could not create output file {}", outputFile.getAbsolutePath(), e);
            return true;
        }

        return false;
    }

    public void arrangeQosToMatchTopics() {
        qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
    }

    public void logUnusedOptions(final @NotNull MqttVersion mqttVersion) {
        if (mqttVersion == MqttVersion.MQTT_3_1_1) {
            if (userProperties != null) {
                Logger.warn("Subscribe user properties were set but are unused in MQTT version {}",
                        MqttVersion.MQTT_3_1_1);
            }
        }
    }

    public void setDefaultOptions() {
        final DefaultCLIProperties defaultCLIProperties = Objects.requireNonNull(MqttCLIMain.MQTTCLI).defaultCLIProperties();

        if (outputFile == null && defaultCLIProperties.getClientSubscribeOutputFile() != null) {
            Logger.trace("Setting value of 'toFile' to {}", defaultCLIProperties.getClientSubscribeOutputFile());
            outputFile = new File(defaultCLIProperties.getClientSubscribeOutputFile());
        }
    }
}
