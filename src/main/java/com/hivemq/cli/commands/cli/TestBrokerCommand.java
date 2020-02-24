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
package com.hivemq.cli.commands.cli;

import com.google.common.base.Joiner;
import com.google.common.primitives.Chars;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.commands.AbstractCommand;
import com.hivemq.cli.commands.options.AuthenticationOptions;
import com.hivemq.cli.commands.options.SslOptions;
import com.hivemq.cli.converters.MqttVersionConverter;
import com.hivemq.cli.mqtt.test.Mqtt3FeatureTester;
import com.hivemq.cli.mqtt.test.Mqtt5FeatureTester;
import com.hivemq.cli.mqtt.test.results.AsciiCharsInClientIdTestResults;
import com.hivemq.cli.mqtt.test.results.ClientIdLengthTestResults;
import com.hivemq.cli.mqtt.test.results.PayloadTestResults;
import com.hivemq.cli.mqtt.test.results.QosTestResult;
import com.hivemq.cli.mqtt.test.results.TopicLengthTestResults;
import com.hivemq.cli.mqtt.test.results.WildcardSubscriptionsTestResult;
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
import org.tinylog.configuration.Configuration;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandLine.Command(
        name = "test",
        description = "Tests the specified broker on different MQTT feature support and prints the results")
public class TestBrokerCommand implements Runnable {

    final int MAX_PAYLOAD_TEST_SIZE = 100000; // ~ 1 MB

    @CommandLine.Option(names = {"-h", "--host"}, description = "The hostname of the message broker (default 'localhost')", order = 1)
    private @Nullable String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "The port of the message broker (default: 1883)", order = 1)
    private @Nullable Integer port;

    @CommandLine.Option(names = {"-V", "--mqttVersion"}, converter = MqttVersionConverter.class, description = "The mqtt version to test the broker on", order = 1)
    private @Nullable MqttVersion version;

    @CommandLine.Option(names = {"-t", "--timeOut"}, defaultValue = "10", description = "The time to wait for the broker to respond", order = 1)
    private @NotNull Integer timeOut;

    @CommandLine.Option(names = {"-q", "--qosTries"}, defaultValue = "10", description = "The amount of publishes to send to the broker on every qos level", order = 1)
    private @NotNull Integer qosTries;

    @CommandLine.Mixin
    private AuthenticationOptions authenticationOptions = new AuthenticationOptions();

    @CommandLine.Mixin
    private SslOptions sslOptions = new SslOptions();

    private @Nullable MqttClientSslConfig sslConfig;

    //needed for pico cli - reflection code generation
    private final DefaultCLIProperties defaultCLIProperties;

    public TestBrokerCommand() { this(null); }

    @Inject
    public TestBrokerCommand(final @NotNull DefaultCLIProperties defaultCLIProperties) {
        this.defaultCLIProperties = defaultCLIProperties;
    }

    @Override
    public void run() {

        // TinyLog configuration
        Map<String, String> configurationMap = new HashMap<String, String>() {{
            put("writer", "console");
            put("writer.format", "{message}");
            put("writer.level", "warn");
        }};

        Configuration.replace(configurationMap);

        if (host == null) { host = defaultCLIProperties.getHost(); }
        if (port == null) { port = defaultCLIProperties.getPort(); }

        try { sslConfig = sslOptions.buildSslConfig(); }
        catch (Exception e) {
            Logger.error(e, "Could not build SSL configuration");
        }

        if (version != null) {
            if (version == MqttVersion.MQTT_3_1_1) { testMqtt3Features(); }
            else if (version == MqttVersion.MQTT_5_0) { testMqtt5Features(); }
        }
        else {
            testMqtt3Features();
            testMqtt5Features();
        }
    }

    public void testMqtt5Features() {
        final Mqtt5FeatureTester tester = new Mqtt5FeatureTester(
                host,
                port,
                authenticationOptions.getUser(),
                authenticationOptions.getPassword(),
                sslConfig
        );

        boolean mqtt5Support = false;

        // Test if MQTT5 is supported
        System.out.print("MQTT 5: ");
        final Mqtt5ConnAck connAck = tester.testConnect();
        if (connAck == null) { System.out.println("NO"); }
        else if (connAck.getReasonCode() == Mqtt5ConnAckReasonCode.SUCCESS) {
            mqtt5Support = true;
            System.out.println("OK");
        }
        else { System.out.println(connAck.getReasonCode().toString()); }

        if (mqtt5Support) {

            //*********************//
            /* Connect Restriction */
            //*********************//

            final Mqtt5ConnAckRestrictions restrictions = connAck.getRestrictions();

            System.out.println("\t- Connect Restrictions: ");

            System.out.print("\t\t> Retain: ");
            System.out.println(restrictions.isRetainAvailable()? "OK" : "NO");

            System.out.print("\t\t> Wildcard subscriptions: ");
            System.out.println(restrictions.isWildcardSubscriptionAvailable()? "OK" : "NO");

            System.out.print("\t\t> Shared subscriptions: ");
            System.out.println(restrictions.isSharedSubscriptionAvailable()? "OK" : "NO");

            System.out.print("\t\t> Subscription identifiers: ");
            System.out.println(restrictions.areSubscriptionIdentifiersAvailable()? "OK" : "NO");

            System.out.print("\t\t> Max. QoS: ");
            System.out.println(restrictions.getMaximumQos().ordinal());

            System.out.print("\t\t> Receive Maximum: ");
            System.out.println(restrictions.getReceiveMaximum());

            System.out.print("\t\t> Maximum packet size: ");
            System.out.println(restrictions.getMaximumPacketSize() + " bytes");

            System.out.print("\t\t> Topic alias maximum: ");
            System.out.println(restrictions.getTopicAliasMaximum());

            System.out.print("\t\t> Session expiry interval: ");
            System.out.println(connAck.getSessionExpiryInterval().isPresent()? connAck.getSessionExpiryInterval().getAsLong() + "s" : "Client-based");

            System.out.print("\t\t> Server keep alive: ");
            System.out.println(connAck.getServerKeepAlive().isPresent()? connAck.getServerKeepAlive().getAsInt() + "s" : "Client-based");

            //**************//
            /* Force Tests */
            //*************//

            // TODO: max topic length
        }
    }

    public void testMqtt3Features() {
        final Mqtt3FeatureTester client = new Mqtt3FeatureTester(
                host,
                port,
                authenticationOptions.getUser(),
                authenticationOptions.getPassword(),
                sslConfig,
                timeOut
        );

        boolean mqtt3Support = false;

        // Test if MQTT3 is supported
        System.out.print("MQTT 3: ");
        final Mqtt3ConnAck connAck = client.testConnect();
        if (connAck == null) { System.out.println("NO"); }
        else if (connAck.getReturnCode() == Mqtt3ConnAckReturnCode.SUCCESS) {
            mqtt3Support = true;
            System.out.println("OK");
        }
        else { System.out.println(connAck.getReturnCode().toString()); }

        if (mqtt3Support) {

            // Test max length of topic names & set length for next tests
            final TopicLengthTestResults topicLengthTestResults = client.testTopicLength();
            final int maxTopicLength = topicLengthTestResults.getMaxTopicLength();
            if (maxTopicLength != 65535) { client.setMaxTopicLength(maxTopicLength); }

            // Test QoS 0
            System.out.print("\t- Testing QoS 0: ");
            final QosTestResult qos0TestResult = client.testQos(MqttQos.AT_MOST_ONCE, qosTries);
            final int qos0Publishes = qos0TestResult.getReceivedPublishes();
            final float qos0Time = qos0TestResult.getTimeToReceivePublishes() / 1_000_000F;
            System.out.printf("Received %d/%d publishes in %.2fms\n", qos0Publishes, qosTries, qos0Time);

            // Test QoS 1
            System.out.print("\t- Testing QoS 1: ");
            final QosTestResult qos1TestResult = client.testQos(MqttQos.AT_LEAST_ONCE, qosTries);
            final int qos1Publishes = qos1TestResult.getReceivedPublishes();
            final float qos1Time = qos1TestResult.getTimeToReceivePublishes() / 1_000_000F;
            System.out.printf("Received %d/%d publishes in %.2fms\n", qos1Publishes, qosTries, qos1Time);

            // Test QoS 2
            System.out.print("\t- Testing QoS 2: ");
            final QosTestResult qos2TestResult  = client.testQos(MqttQos.EXACTLY_ONCE, qosTries);
            final int qos2Publishes = qos2TestResult.getReceivedPublishes();
            final float qos2Time = qos2TestResult.getTimeToReceivePublishes() / 1_000_000F;
            System.out.printf("Received %d/%d publishes in %.2fms\n", qos2Publishes, qosTries, qos2Time);

            // Test retain
            System.out.print("\t- Retain: ");
            System.out.println(client.testRetain());

            // Test if wildcard subscriptions are allowed
            System.out.print("\t- Wildcard subscriptions: ");
            final WildcardSubscriptionsTestResult wildcardSubscriptionsTestResult = client.testWildcardSubscriptions();
            if (wildcardSubscriptionsTestResult.isSuccess()) { System.out.println("OK"); }
            else {
                System.out.println("NO");
                System.out.print("\t\t> '+' Wildcard: ");
                System.out.println(wildcardSubscriptionsTestResult.getPlusWildcardTest());
                System.out.print("\t\t> '#' Wildcard: ");
                System.out.println(wildcardSubscriptionsTestResult.getHashWildcardTest());
            }

            // Test max payload size
            System.out.print("\t- Payload size: ");
            final PayloadTestResults payloadTestResults = client.testPayloadSize(MAX_PAYLOAD_TEST_SIZE);
            final int payloadSize = payloadTestResults.getPayloadSize();
            if (payloadSize == MAX_PAYLOAD_TEST_SIZE) { System.out.println(">= " + payloadSize + " bytes"); }
            else { System.out.println(payloadSize + " bytes"); }

            // Test max client id length
            System.out.print("\t- Max. client id length: ");
            final ClientIdLengthTestResults clientIdLengthTestResults = client.testClientIdLength();
            final int maxClientIdLength = clientIdLengthTestResults.getMaxClientIdLength();
            System.out.println(maxClientIdLength + " bytes");

            // Print max topic length
            System.out.print("\t- Max. topic length: ");
            System.out.println(maxTopicLength + " bytes");

            // Test supported Ascii chars
            System.out.print("\t- Unsupported Ascii Chars: ");
            final AsciiCharsInClientIdTestResults asciiTestResults = client.testAsciiCharsInClientId();
            final List<Character> unsupportedChars = asciiTestResults.getUnsupportedChars();
            if (unsupportedChars.isEmpty()) { System.out.println("ALL SUPPORTED"); }
            else { System.out.println("{'" + Joiner.on("', '").join(unsupportedChars) + "'}"); }
        }

    }

}
