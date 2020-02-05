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

import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.commands.AbstractCommand;
import com.hivemq.cli.mqtt.test.Mqtt3TestClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(
        name = "test",
        description = "Tests the specified broker on different MQTT feature support and prints the results")
public class TestBrokerCommand extends AbstractCommand implements Runnable {

    @CommandLine.Option(names = {"-h", "--host"}, description = "The hostname of the message broker (default 'localhost')", order = 1)
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "The port of the message broker (default: 1883)", order = 1)
    private Integer port;

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
        final Mqtt3TestClient client = new Mqtt3TestClient(host, port);
        boolean mqtt3Support = false;

        // Test if MQTT3 is supported
        System.out.print("MQTT 3: ");
        final Mqtt3ConnAck connAck = client.connect();
        if (connAck == null) { System.out.println("NO"); }
        else if (connAck.getReturnCode() == Mqtt3ConnAckReturnCode.SUCCESS) {
            mqtt3Support = true;
            System.out.println("OK");
        }
        else { System.out.println(connAck.getReturnCode().toString()); }

        if (mqtt3Support) {
            // Test if wildcard subscriptions are allowed
            System.out.print("\t- Wildcard subscriptions: ");
            final Mqtt3SubAck subAck = client.testWildcardSubscription();
            if (subAck == null) { System.out.println("NO"); }
            else if (!subAck.getReturnCodes().contains(Mqtt3SubAckReturnCode.FAILURE)) { System.out.println("OK"); }
            else { System.out.println("NO"); }

            // Test max length of topic names
            System.out.print("\t- Max. topic length: ");
            final int maxTopicLength = client.testTopicLength();
            System.out.println(maxTopicLength + " bytes");
        }

    }

}
