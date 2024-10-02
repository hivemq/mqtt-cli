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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SubscribeOptions {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Spec
    private @NotNull CommandLine.Model.CommandSpec spec;

    private final @NotNull List<String> deprecationWarnings;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via required
    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to subscribe to")
    private @NotNull String @NotNull [] topics;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via default value
    @CommandLine.Option(names = {"-q", "--qos"},
                        converter = MqttQosConverter.class,
                        defaultValue = "2",
                        description = "Quality of service for the corresponding topics (default for all: 2)")
    private @NotNull MqttQos @NotNull [] qos;

    private @NotNull Mqtt5UserProperty @Nullable [] userProperties = null;
    private boolean userPropertiesFromOption = false;
    private boolean userPropertiesFromLegacyOption = false;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--user-property"},
                        converter = Mqtt5UserPropertyConverter.class,
                        description = "A user property of the subscribe message")
    private void userProperties(final @NotNull Mqtt5UserProperty @NotNull [] userProperties) {
        if (userPropertiesFromLegacyOption) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "A mix of the user properties legacy options \"-up\" or \"--userProperty\" and the current \"--user-property\" is used. Please only use \"--user-property\" as the legacy options will be removed in a future version.");
        }
        userPropertiesFromOption = true;
        this.userProperties = userProperties;
    }

    @SuppressWarnings("unused")
    @Deprecated(since = "4.34.0", forRemoval = true)
    @CommandLine.Option(names = {"-up", "--userProperty"},
                        hidden = true,
                        converter = Mqtt5UserPropertyConverter.class,
                        description = "Options \"-up\" and \"--userProperty\" are legacy, please use \"--user-property\". They will be removed in a future version.")
    private void userPropertiesLegacy(final @NotNull Mqtt5UserProperty @NotNull [] userPropertiesLegacy) {
        //only show message once as this method is executed multiple times
        if (!userPropertiesFromLegacyOption) {
            deprecationWarnings.add(
                    "Options \"-up\" and \"--userProperty\" are legacy, please use \"--user-property\". They will be removed in a future version.");
        }

        if (userPropertiesFromOption) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "A mix of the user properties legacy options \"-up\" or \"--userProperty\" and the current \"--user-property\" is used. Please only use \"--user-property\" as the legacy options will be removed in a future version.");
        }
        userPropertiesFromLegacyOption = true;
        userProperties = userPropertiesLegacy;
    }

    private @Nullable File outputFile = null;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--output-to-file"},
                        description = "A file to which the received publish messages will be written")
    private void outputFile(final @NotNull File outputFile) {
        if (this.outputFile != null) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "A mix of the output file legacy options \"-of\" or \"--outputToFile\" and the current \"--output-to-file\" is used. Please only use \"--output-to-file\" as the legacy options will be removed in a future version.");
        }
        this.outputFile = outputFile;
    }

    @SuppressWarnings("unused")
    @Deprecated(since = "4.34.0", forRemoval = true)
    @CommandLine.Option(names = {"-of", "--outputToFile"},
                        hidden = true,
                        description = "Options \"-of\" and \"--outputToFile\" are legacy, please use \"--output-to-file\". They will be removed in a future version")
    private void outputFileLegacy(final @NotNull File outputFileLegacy) {
        deprecationWarnings.add(
                "Options \"-of\" and \"--outputToFile\" are legacy, please use \"--output-to-file\". They will be removed in a future version.");

        if (this.outputFile != null) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "A mix of the output file legacy options \"-of\" or \"--outputToFile\" and the current \"--output-to-file\" is used. Please only use \"--output-to-file\" as the legacy options will be removed in a future version.");
        }
        this.outputFile = outputFileLegacy;
    }

    private boolean isPayloadEncodeToBase64 = false;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--base64"},
                        description = "Specify the encoding of the received messages as Base64 (default: false)")
    private void isMessageBase64Encoded(final boolean base64) {
        if (isPayloadEncodeToBase64) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "A mix of the base64 legacy options \"-b64\" and the current \"--base64\" is used. Please only use \"--base64\" as the legacy options will be removed in a future version.");
        }
        isPayloadEncodeToBase64 = base64;
    }

    @SuppressWarnings("unused")
    @Deprecated(since = "4.34.0", forRemoval = true)
    @CommandLine.Option(names = {"-b64"},
                        hidden = true,
                        description = "Option \"-b64\" is legacy, please use \"--base64\". It will be removed in a future version")
    private void isMessageBase64EncodedLegacy(final boolean base64Legacy) {
        deprecationWarnings.add(
                "Option \"-b64\" is legacy, please use \"--base64\". It will be removed in a future version.");

        if (isPayloadEncodeToBase64) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "A mix of the base64 legacy options \"-b64\" and the current \"--base64\" is used. Please only use \"--base64\" as the legacy options will be removed in a future version.");
        }
        isPayloadEncodeToBase64 = base64Legacy;
    }

    private boolean jsonOutput = false;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-J", "--json-output"},
                        description = "Print the received publishes in pretty JSON format")
    private void jsonOutput(final boolean jsonOutput) {
        if (this.jsonOutput) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "A mix of the json output legacy options \"--jsonOutput\" and the current \"-J\" or \"--json-output\" is used. Please only use \"-J\" or \"--json-output\" as the legacy options will be removed in a future version.");
        }
        this.jsonOutput = jsonOutput;
    }

    @SuppressWarnings("unused")
    @Deprecated(since = "4.34.0", forRemoval = true)
    @CommandLine.Option(names = {"--jsonOutput"},
                        hidden = true,
                        description = "Option \"--jsonOutput\" is legacy, please use \"--json-output\". It will be removed in a future version")
    private void jsonOutputLegacy(final boolean jsonOutputLegacy) {
        deprecationWarnings.add(
                "Option \"--jsonOutput\" is legacy, please use \"--json-output\". It will be removed in a future version.");

        if (jsonOutput) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "A mix of the json output legacy options \"--jsonOutput\" and the current \"-J\" or \"--json-output\" is used. Please only use \"-J\" or \"--json-output\" as the legacy options will be removed in a future version.");
        }
        jsonOutput = true;
    }

    private boolean showTopics = false;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-T", "--show-topics"},
                        description = "Prepend the specific topic name to the received publish")
    private void showTopics(final boolean showTopics) {
        if (this.showTopics) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "A mix of the show topics legacy options \"--showTopics\" and the current \"-T\" or \"--show-topics\" is used. Please only use \"-T\" or \"--show-topics\" as the legacy options will be removed in a future version.");
        }
        this.showTopics = showTopics;
    }

    @SuppressWarnings("unused")
    @Deprecated(since = "4.34.0", forRemoval = true)
    @CommandLine.Option(names = {"--showTopics"},
                        hidden = true,
                        description = "Option \"--showTopics\" is legacy, please use \"-T\" or \"--show-topics\". It will be removed in a future version")
    private void showTopicsLegacy(final boolean showTopicsLegacy) {
        deprecationWarnings.add(
                "Option \"--showTopics\" is legacy, please use \"-T\" or \"--show-topics\". It will be removed in a future version.");

        if (this.showTopics) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "A mix of the show topics legacy options \"--showTopics\" and the current \"-T\" or \"--show-topics\" is used. Please only use \"-T\" or \"--show-topics\" as the legacy options will be removed in a future version.");
        }
        showTopics = true;
    }

    private boolean printToSTDOUT = false;

    public SubscribeOptions(final @NotNull List<String> deprecationWarnings) {
        this.deprecationWarnings = deprecationWarnings;
        setDefaultOptions();
    }

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

    public boolean isPayloadEncodeToBase64() {
        return isPayloadEncodeToBase64;
    }

    public boolean isJsonOutput() {
        return jsonOutput;
    }

    public @Nullable Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    public @Nullable Mqtt5UserProperty @Nullable [] getUserPropertiesRaw() {
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
        final DefaultCLIProperties defaultCLIProperties =
                Objects.requireNonNull(MqttCLIMain.MQTT_CLI).defaultCLIProperties();

        if (outputFile == null && defaultCLIProperties.getClientSubscribeOutputFile() != null) {
            Logger.trace("Setting value of 'toFile' to {}", defaultCLIProperties.getClientSubscribeOutputFile());
            outputFile = new File(defaultCLIProperties.getClientSubscribeOutputFile());
        }
    }

    @Override
    public @NotNull String toString() {
        return "SubscribeOptions{" +
                "topics=" +
                Arrays.toString(topics) +
                ", qos=" +
                Arrays.toString(qos) +
                ", userProperties=" +
                Arrays.toString(userProperties) +
                ", outputFile=" +
                outputFile +
                ", printToSTDOUT=" +
                printToSTDOUT +
                ", base64=" +
                isPayloadEncodeToBase64 +
                ", jsonOutput=" +
                jsonOutput +
                ", showTopics=" +
                showTopics +
                '}';
    }
}
