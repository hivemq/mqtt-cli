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

package com.hivemq.cli.commands.hivemq.export.clients;

import com.hivemq.cli.utils.TestLoggerUtils;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import picocli.CommandLine;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportClientsCommandIT {

    public static final int HTTP_PORT = 8888;

    @RegisterExtension
    final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq4")).withHiveMQConfig(MountableFile.forClasspathResource(
                            "hivemq.configs/rest-api-config.xml"))
                    .withExposedPorts(HiveMQTestContainerExtension.MQTT_PORT, HTTP_PORT);

    private @NotNull File file;

    @BeforeEach
    void setUp() throws IOException {
        TestLoggerUtils.resetLogger();
        file = File.createTempFile("client_details", ".csv");
    }

    @AfterEach
    void tearDown() {
        file.deleteOnExit();
    }

    @Test
    void client_detail_1_success() throws IOException, CsvException {
        final Mqtt5BlockingClient client = Mqtt5Client.builder().serverPort(hivemq.getMqttPort()).buildBlocking();

        client.connect();

        final CommandLine cmd = new CommandLine(new ExportClientsCommand());
        final int returnCode = cmd.execute("-f=" + file.getAbsolutePath(),
                "-url=http://" + hivemq.getHost() + ":" + hivemq.getMappedPort(HTTP_PORT));

        assertEquals(0, returnCode);

        final CSVReader csvReader = new CSVReader(new FileReader(file));
        final List<String[]> lines = csvReader.readAll();
        assertEquals(2, lines.size());

        client.disconnect();
    }

    @Test
    void client_details_25_success() throws IOException, CsvException {
        final Mqtt5BlockingClient[] clients = new Mqtt5BlockingClient[25];
        for (int i = 0; i < 25; i++) {
            clients[i] = Mqtt5Client.builder().serverPort(hivemq.getMqttPort()).buildBlocking();
            clients[i].connect();
        }

        final CommandLine cmd = new CommandLine(new ExportClientsCommand());
        final int returnCode = cmd.execute("-f=" + file.getAbsolutePath(),
                "-url=http://" + hivemq.getHost() + ":" + hivemq.getMappedPort(HTTP_PORT));

        assertEquals(0, returnCode);

        final CSVReader csvReader = new CSVReader(new FileReader(file));
        final List<String[]> lines = csvReader.readAll();
        assertEquals(26, lines.size());

        for (int i = 0; i < 25; i++) {
            clients[i].disconnect();
        }
    }

    @Test
    void csv_options() throws IOException, CsvException {
        final Mqtt5BlockingClient client = Mqtt5Client.builder().serverPort(hivemq.getMqttPort()).buildBlocking();

        client.connect();

        final CommandLine cmd = new CommandLine(new ExportClientsCommand());
        final int returnCode = cmd.execute("-f=" + file.getAbsolutePath(),
                "-url=http://" + hivemq.getHost() + ":" + hivemq.getMappedPort(HTTP_PORT),
                "--csvSeparator=;",
                "--csvQuoteChar=\\",
                "--csvEscChar=/");

        assertEquals(0, returnCode);

        final CSVParser parser =
                new CSVParserBuilder().withSeparator(';').withEscapeChar('/').withQuoteChar('\\').build();

        final CSVReader csvReader = new CSVReaderBuilder(new FileReader(file)).withCSVParser(parser).build();

        final List<String[]> lines = csvReader.readAll();
        assertEquals(2, lines.size());

        client.disconnect();
    }

    @Test
    void rate_limit() throws IOException, CsvException {
        final Mqtt5BlockingClient[] clients = new Mqtt5BlockingClient[25];
        for (int i = 0; i < 10; i++) {
            clients[i] = Mqtt5Client.builder().serverPort(hivemq.getMqttPort()).buildBlocking();
            clients[i].connect();
        }

        final long startTime = System.nanoTime();
        final CommandLine cmd = new CommandLine(new ExportClientsCommand());
        final int returnCode = cmd.execute("-f=" + file.getAbsolutePath(),
                "-url=http://" + hivemq.getHost() + ":" + hivemq.getMappedPort(HTTP_PORT),
                "-r=5");
        final long stopTime = System.nanoTime();

        assertTrue(((stopTime - startTime) / 1_000_000_000) >= 2);
        assertEquals(0, returnCode);

        final CSVReader csvReader = new CSVReader(new FileReader(file));
        final List<String[]> lines = csvReader.readAll();
        assertEquals(11, lines.size());

        for (int i = 0; i < 10; i++) {
            clients[i].disconnect();
        }
    }

    @Test
    void connection_refused() {
        final Mqtt5BlockingClient client = Mqtt5Client.builder().serverPort(hivemq.getMqttPort()).buildBlocking();

        client.connect();

        final CommandLine cmd = new CommandLine(new ExportClientsCommand());
        final int returnCode =
                cmd.execute("-f=" + file.getAbsolutePath(), "-url=http://" + hivemq.getHost() + ":" + 8889);

        assertEquals(-1, returnCode);

        client.disconnect();
    }
}
