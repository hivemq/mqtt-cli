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
import com.hivemq.cli.mqtt.test.Mqtt3FeatureTester;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(
        name = "test",
        description = "Tests the specified broker on different MQTT feature support and prints the results")
public class TestBrokerCommand extends AbstractCommand implements Runnable {

    final int MAX_PAYLOAD_TEST_SIZE = 100000; // ~ 1 MB

    @CommandLine.Option(names = {"-h", "--host"}, description = "The hostname of the message broker (default 'localhost')", order = 1)
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "The port of the message broker (default: 1883)", order = 1)
    private Integer port;

    @CommandLine.Mixin
    private AuthenticationOptions authenticationOptions = new AuthenticationOptions();

    @CommandLine.Mixin
    private SslOptions sslOptions = new SslOptions();

    //needed for pico cli - reflection code generation
    private final DefaultCLIProperties defaultCLIProperties;

    public TestBrokerCommand() { this(null); }

    @Inject
    public TestBrokerCommand(final @NotNull DefaultCLIProperties defaultCLIProperties) {
        this.defaultCLIProperties = defaultCLIProperties;
    }


    @Override
    public void run() {
        if (host == null) { host = defaultCLIProperties.getHost(); }
        if (port == null) { port = defaultCLIProperties.getPort(); }

        testMqtt3Features();
    }

    public void testMqtt3Features() {
        final Mqtt3FeatureTester client = new Mqtt3FeatureTester(host,
                port,
                authenticationOptions.getUser(),
                authenticationOptions.getPassword());

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
            final int maxTopicLength = client.testTopicLength();
            if (maxTopicLength != 65535) { client.setMaxTopicLength(maxTopicLength); }

            // Test if wildcard subscriptions are allowed
            System.out.print("\t- Wildcard subscriptions: ");
            System.out.println(client.testWildcardSubscriptions()? "OK" : "NO");

            // Test retain
            System.out.print("\t- Retain: ");
            System.out.println(client.testRetain() ? "OK" : "NO");

            // Test max payload size
            System.out.print("\t- Payload size: ");
            final int payloadSize = client.testPayloadSize(MAX_PAYLOAD_TEST_SIZE);
            if (payloadSize == MAX_PAYLOAD_TEST_SIZE) { System.out.println(">= " + payloadSize + " bytes"); }
            else { System.out.println(payloadSize + " bytes"); }

            // Test max client id length
            System.out.print("\t- Max. client id length: ");
            final int maxClientIdLength = client.testClientIdLength();
            System.out.println(maxClientIdLength + " bytes");

            // Print max topic length
            System.out.print("\t- Max. topic length: ");
            System.out.println(maxTopicLength + " bytes");

            // Test supported Ascii chars
            System.out.print("\t- Unsupported Ascii Chars: ");
            final String unsupportedChars = client.testClientIdAsciiChars();
            if (unsupportedChars.isEmpty()) { System.out.println("ALL SUPPORTED"); }
            else { System.out.println("{'" + Joiner.on("', '").join(Chars.asList(unsupportedChars.toCharArray())) + "'}"); }


        }

    }

}
