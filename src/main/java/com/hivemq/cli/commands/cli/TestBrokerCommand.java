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

package com.hivemq.cli.commands.cli;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.commands.options.AuthenticationOptions;
import com.hivemq.cli.commands.options.HelpOptions;
import com.hivemq.cli.commands.options.TlsOptions;
import com.hivemq.cli.converters.MqttVersionConverter;
import com.hivemq.cli.mqtt.test.Mqtt3FeatureTester;
import com.hivemq.cli.mqtt.test.Mqtt5FeatureTester;
import com.hivemq.cli.mqtt.test.results.AsciiCharsInClientIdTestResults;
import com.hivemq.cli.mqtt.test.results.ClientIdLengthTestResults;
import com.hivemq.cli.mqtt.test.results.PayloadTestResults;
import com.hivemq.cli.mqtt.test.results.QosTestResult;
import com.hivemq.cli.mqtt.test.results.SharedSubscriptionTestResult;
import com.hivemq.cli.mqtt.test.results.TopicLengthTestResults;
import com.hivemq.cli.mqtt.test.results.WildcardSubscriptionsTestResult;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckRestrictions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "test",
                     description = "Tests the specified broker on different MQTT feature support and prints the results.",
                     sortOptions = false)
public class TestBrokerCommand implements Callable<Integer> {

    private static final int MAX_PAYLOAD_TEST_SIZE = 100000; // ~ 1 MB

    @CommandLine.Option(names = {"-h", "--host"},
                        description = "The hostname of the message broker (default 'localhost')")
    private @Nullable String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "The port of the message broker (default: 1883)")
    private @Nullable Integer port;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-V", "--mqttVersion"},
                        converter = MqttVersionConverter.class,
                        description = "The MQTT version to test the broker on (default: test both versions)")
    private @Nullable MqttVersion version;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-a", "--all"},
                        defaultValue = "false",
                        description = "Perform all tests for all MQTT versions (default: only MQTT 3)")
    private boolean testAll;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via default value
    @CommandLine.Option(names = {"-t", "--timeOut"},
                        defaultValue = "10",
                        description = "The time to wait for the broker to respond")
    private @NotNull Integer timeOut;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via default value
    @CommandLine.Option(names = {"-q", "--qosTries"},
                        defaultValue = "10",
                        description = "The amount of publishes to send to the broker on every qos level")
    private @NotNull Integer qosTries;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-l"},
                        defaultValue = "false",
                        description = "Log to $HOME/.mqtt-cli/logs (Configurable through $HOME/.mqtt-cli/config.properties)")
    private boolean logToLogfile;

    @CommandLine.Mixin
    private final @NotNull AuthenticationOptions authenticationOptions = new AuthenticationOptions();

    @CommandLine.Mixin
    private final @NotNull TlsOptions tlsOptions = new TlsOptions();

    @CommandLine.Mixin
    private final @NotNull HelpOptions helpOptions = new HelpOptions();

    private final @NotNull DefaultCLIProperties defaultCLIProperties;

    private @Nullable MqttClientSslConfig sslConfig;

    @Inject
    public TestBrokerCommand(final @NotNull DefaultCLIProperties defaultCLIProperties) {
        this.defaultCLIProperties = defaultCLIProperties;
    }

    @Override
    public @NotNull Integer call() {
        LoggerUtils.turnOffConsoleLogging(logToLogfile);

        Logger.trace("Command {}", this);

        if (host == null) {
            host = defaultCLIProperties.getHost();
        }
        if (port == null) {
            port = defaultCLIProperties.getPort();
        }

        authenticationOptions.setDefaultOptions();

        try {
            sslConfig = tlsOptions.buildSslConfig();
        } catch (final Exception e) {
            Logger.error(e, "Could not build SSL configuration");
            System.err.println("Could not build SSL config - " + Throwables.getRootCause(e).getMessage());
            return 1;
        }

        int mqtt3ExitCode = 0;
        int mqtt5ExitCode = 0;
        if (version != null) {
            if (version == MqttVersion.MQTT_3_1_1) {
                mqtt3ExitCode = testMqtt3Features();
            } else if (version == MqttVersion.MQTT_5_0) {
                mqtt5ExitCode = testMqtt5Features();
            }
        } else {
            mqtt3ExitCode = testMqtt3Features();
            mqtt5ExitCode = testMqtt5Features();
        }

        if (mqtt3ExitCode != 0 || mqtt5ExitCode != 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public int testMqtt5Features() {
        final Mqtt5FeatureTester mqtt5Tester = new Mqtt5FeatureTester(Objects.requireNonNull(host),
                Objects.requireNonNull(port),
                authenticationOptions.getUser(),
                authenticationOptions.getPassword(),
                sslConfig,
                timeOut);

        Logger.info("Testing MQTT 5");

        // Test if MQTT5 is supported
        System.out.print("MQTT 5: ");

        final Mqtt5ConnAck connAck;
        try {
            connAck = mqtt5Tester.testConnect();
        } catch (final Exception e) {
            Logger.error(e, "Could not connect MQTT 5 client");
            System.err.println("Could not connect MQTT 5 client - " + Throwables.getRootCause(e).getMessage());
            return 1;
        }

        if (connAck == null) {
            System.out.println("NO");
            return 1;
        } else if (connAck.getReasonCode() != Mqtt5ConnAckReasonCode.SUCCESS) {
            System.out.println(connAck.getReasonCode());
            return 1;
        } else {
            System.out.println("OK");
        }

        final Mqtt5ConnAckRestrictions restrictions = connAck.getRestrictions();

        System.out.println("\t- Connect restrictions: ");

        System.out.print("\t\t> Retain: ");
        System.out.println(restrictions.isRetainAvailable() ? "OK" : "NO");

        System.out.print("\t\t> Wildcard subscriptions: ");
        System.out.println(restrictions.isWildcardSubscriptionAvailable() ? "OK" : "NO");

        System.out.print("\t\t> Shared subscriptions: ");
        System.out.println(restrictions.isSharedSubscriptionAvailable() ? "OK" : "NO");

        System.out.print("\t\t> Subscription identifiers: ");
        System.out.println(restrictions.areSubscriptionIdentifiersAvailable() ? "OK" : "NO");

        System.out.print("\t\t> Maximum QoS: ");
        System.out.println(restrictions.getMaximumQos().getCode());

        System.out.print("\t\t> Receive maximum: ");
        System.out.println(restrictions.getReceiveMaximum());

        System.out.print("\t\t> Maximum packet size: ");
        System.out.println(restrictions.getMaximumPacketSize() + " bytes");

        System.out.print("\t\t> Topic alias maximum: ");
        System.out.println(restrictions.getTopicAliasMaximum());

        System.out.print("\t\t> Session expiry interval: ");
        System.out.println(connAck.getSessionExpiryInterval().isPresent() ?
                connAck.getSessionExpiryInterval().getAsLong() + "s" :
                "Client-based");

        System.out.print("\t\t> Server keep alive: ");
        System.out.println(connAck.getServerKeepAlive().isPresent() ?
                connAck.getServerKeepAlive().getAsInt() + "s" :
                "Client-based");


        if (testAll) {
            // Print max topic length
            System.out.print("\t- Maximum topic length: ");
            final TopicLengthTestResults topicLengthTestResults = mqtt5Tester.testTopicLength();
            final int maxTopicLength = topicLengthTestResults.getMaxTopicLength();
            System.out.println(maxTopicLength + " bytes");

            // Test QoS 0
            System.out.print("\t- QoS 0: ");
            final QosTestResult qos0TestResult = mqtt5Tester.testQos(MqttQos.AT_MOST_ONCE, qosTries);
            final int qos0Publishes = qos0TestResult.getReceivedPublishes();
            final float qos0Time = qos0TestResult.getTimeToReceivePublishes() / 1_000_000F;
            System.out.printf("Received %d/%d publishes in %.2fms\n", qos0Publishes, qosTries, qos0Time);

            // Test QoS 1
            System.out.print("\t- QoS 1: ");
            final QosTestResult qos1TestResult = mqtt5Tester.testQos(MqttQos.AT_LEAST_ONCE, qosTries);
            final int qos1Publishes = qos1TestResult.getReceivedPublishes();
            final float qos1Time = qos1TestResult.getTimeToReceivePublishes() / 1_000_000F;
            System.out.printf("Received %d/%d publishes in %.2fms\n", qos1Publishes, qosTries, qos1Time);

            // Test QoS 2
            System.out.print("\t- QoS 2: ");
            final QosTestResult qos2TestResult = mqtt5Tester.testQos(MqttQos.EXACTLY_ONCE, qosTries);
            final int qos2Publishes = qos2TestResult.getReceivedPublishes();
            final float qos2Time = qos2TestResult.getTimeToReceivePublishes() / 1_000_000F;
            System.out.printf("Received %d/%d publishes in %.2fms\n", qos2Publishes, qosTries, qos2Time);

            // Test retain
            System.out.print("\t- Retain: ");
            System.out.println(mqtt5Tester.testRetain());

            // Test if wildcard subscriptions are allowed
            System.out.print("\t- Wildcard subscriptions: ");
            final WildcardSubscriptionsTestResult wildcardSubscriptionsTestResult =
                    mqtt5Tester.testWildcardSubscriptions();
            if (wildcardSubscriptionsTestResult.isSuccess()) {
                System.out.println("OK");
            } else {
                System.out.println("NO");
                System.out.print("\t\t> '+' Wildcard: ");
                System.out.println(wildcardSubscriptionsTestResult.getPlusWildcardTest());
                System.out.print("\t\t> '#' Wildcard: ");
                System.out.println(wildcardSubscriptionsTestResult.getHashWildcardTest());
            }

            System.out.print("\t- Shared subscriptions: ");
            final SharedSubscriptionTestResult sharedSubscriptionTestResult = mqtt5Tester.testSharedSubscription();
            System.out.println(sharedSubscriptionTestResult);

            // Test max payload size
            System.out.print("\t- Payload size: ");
            final PayloadTestResults payloadTestResults = mqtt5Tester.testPayloadSize(MAX_PAYLOAD_TEST_SIZE);
            final int payloadSize = payloadTestResults.getPayloadSize();
            if (payloadSize == MAX_PAYLOAD_TEST_SIZE) {
                System.out.println(">= " + payloadSize + " bytes");
            } else {
                System.out.println(payloadSize + " bytes");
            }

            // Test max client id length
            System.out.print("\t- Maximum client id length: ");
            final ClientIdLengthTestResults clientIdLengthTestResults = mqtt5Tester.testClientIdLength();
            final int maxClientIdLength = clientIdLengthTestResults.getMaxClientIdLength();
            System.out.println(maxClientIdLength + " bytes");

            // Test supported Ascii chars
            System.out.print("\t- Unsupported Ascii Chars: ");
            final AsciiCharsInClientIdTestResults asciiTestResults = mqtt5Tester.testAsciiCharsInClientId();
            final List<Character> unsupportedChars = asciiTestResults.getUnsupportedChars();
            if (unsupportedChars.isEmpty()) {
                System.out.println("ALL SUPPORTED");
            } else {
                System.out.println("{'" + Joiner.on("', '").join(unsupportedChars) + "'}");
            }
        }

        Logger.info("Finished testing MQTT 5");
        return 0;
    }

    public int testMqtt3Features() {
        final Mqtt3FeatureTester mqtt3Tester = new Mqtt3FeatureTester(Objects.requireNonNull(host),
                Objects.requireNonNull(port),
                authenticationOptions.getUser(),
                authenticationOptions.getPassword(),
                sslConfig,
                timeOut);

        Logger.info("Testing MQTT 3");

        // Test if MQTT3 is supported
        System.out.print("MQTT 3: ");

        final Mqtt3ConnAck connAck;
        try {
            connAck = mqtt3Tester.testConnect();
        } catch (final Exception e) {
            Logger.error(e, "Could not connect MQTT 3 client");
            System.err.println("Could not connect MQTT 3 client - " + Throwables.getRootCause(e).getMessage());
            return 1;
        }
        if (connAck == null) {
            System.out.println("NO");
            return 1;
        } else if (connAck.getReturnCode() != Mqtt3ConnAckReturnCode.SUCCESS) {
            System.out.println(connAck.getReturnCode());
            return 1;
        } else {
            System.out.println("OK");
        }

        // Test max length of topic names & set length for next tests
        System.out.print("\t- Maximum topic length: ");
        final TopicLengthTestResults topicLengthTestResults = mqtt3Tester.testTopicLength();
        final int maxTopicLength = topicLengthTestResults.getMaxTopicLength();
        System.out.println(maxTopicLength + " bytes");

        // Test QoS 0
        System.out.print("\t- QoS 0: ");
        final QosTestResult qos0TestResult = mqtt3Tester.testQos(MqttQos.AT_MOST_ONCE, qosTries);
        final int qos0Publishes = qos0TestResult.getReceivedPublishes();
        final float qos0Time = qos0TestResult.getTimeToReceivePublishes() / 1_000_000F;
        System.out.printf("Received %d/%d publishes in %.2fms\n", qos0Publishes, qosTries, qos0Time);

        // Test QoS 1
        System.out.print("\t- QoS 1: ");
        final QosTestResult qos1TestResult = mqtt3Tester.testQos(MqttQos.AT_LEAST_ONCE, qosTries);
        final int qos1Publishes = qos1TestResult.getReceivedPublishes();
        final float qos1Time = qos1TestResult.getTimeToReceivePublishes() / 1_000_000F;
        System.out.printf("Received %d/%d publishes in %.2fms\n", qos1Publishes, qosTries, qos1Time);

        // Test QoS 2
        System.out.print("\t- QoS 2: ");
        final QosTestResult qos2TestResult = mqtt3Tester.testQos(MqttQos.EXACTLY_ONCE, qosTries);
        final int qos2Publishes = qos2TestResult.getReceivedPublishes();
        final float qos2Time = qos2TestResult.getTimeToReceivePublishes() / 1_000_000F;
        System.out.printf("Received %d/%d publishes in %.2fms\n", qos2Publishes, qosTries, qos2Time);

        // Test retain
        System.out.print("\t- Retain: ");
        System.out.println(mqtt3Tester.testRetain());

        // Test if wildcard subscriptions are allowed
        System.out.print("\t- Wildcard subscriptions: ");
        final WildcardSubscriptionsTestResult wildcardSubscriptionsTestResult = mqtt3Tester.testWildcardSubscriptions();
        if (wildcardSubscriptionsTestResult.isSuccess()) {
            System.out.println("OK");
        } else {
            System.out.println("NO");
            System.out.print("\t\t> '+' Wildcard: ");
            System.out.println(wildcardSubscriptionsTestResult.getPlusWildcardTest());
            System.out.print("\t\t> '#' Wildcard: ");
            System.out.println(wildcardSubscriptionsTestResult.getHashWildcardTest());
        }

        System.out.print("\t- Shared subscriptions: ");
        final SharedSubscriptionTestResult sharedSubscriptionTestResult = mqtt3Tester.testSharedSubscription();
        System.out.println(sharedSubscriptionTestResult);

        // Test max payload size
        System.out.print("\t- Payload size: ");
        final PayloadTestResults payloadTestResults = mqtt3Tester.testPayloadSize(MAX_PAYLOAD_TEST_SIZE);
        final int payloadSize = payloadTestResults.getPayloadSize();
        if (payloadSize == MAX_PAYLOAD_TEST_SIZE) {
            System.out.println(">= " + payloadSize + " bytes");
        } else {
            System.out.println(payloadSize + " bytes");
        }

        // Test max client id length
        System.out.print("\t- Maximum client id length: ");
        final ClientIdLengthTestResults clientIdLengthTestResults = mqtt3Tester.testClientIdLength();
        final int maxClientIdLength = clientIdLengthTestResults.getMaxClientIdLength();
        System.out.println(maxClientIdLength + " bytes");

        // Test supported Ascii chars
        System.out.print("\t- Unsupported Ascii Chars: ");
        final AsciiCharsInClientIdTestResults asciiTestResults = mqtt3Tester.testAsciiCharsInClientId();
        final List<Character> unsupportedChars = asciiTestResults.getUnsupportedChars();
        if (unsupportedChars.isEmpty()) {
            System.out.println("ALL SUPPORTED");
        } else {
            System.out.println("{'" + Joiner.on("', '").join(unsupportedChars) + "'}");
        }

        Logger.info("Finished testing MQTT 3");
        return 0;
    }

    @Override
    public @NotNull String toString() {
        return "TestBrokerCommand{" +
                "host='" +
                host +
                '\'' +
                ", port=" +
                port +
                ", version=" +
                version +
                ", testAll=" +
                testAll +
                ", timeOut=" +
                timeOut +
                ", qosTries=" +
                qosTries +
                ", logToLogfile=" +
                logToLogfile +
                ", authenticationOptions=" +
                authenticationOptions +
                ", tlsOptions=" +
                tlsOptions +
                ", helpOptions=" + helpOptions +
                ", defaultCLIProperties=" +
                defaultCLIProperties +
                ", sslConfig=" +
                sslConfig +
                '}';
    }
}
