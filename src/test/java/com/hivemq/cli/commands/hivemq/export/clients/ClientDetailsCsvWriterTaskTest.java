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

import com.hivemq.cli.openapi.hivemq.*;
import com.hivemq.cli.rest.hivemq.TestClientDetails;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static com.hivemq.cli.commands.hivemq.export.clients.ClientDetailsCsvWriterTask.EXPORT_CSV_HEADER;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientDetailsCsvWriterTaskTest {

    private @NotNull CompletableFuture<Void> clientDetailsFuture;
    private @NotNull File csvFile;
    private @NotNull BlockingQueue<ClientDetails> clientDetailsQueue;
    private @NotNull ClientDetailsCsvWriterTask clientDetailsCsvWriterTask;
    private @NotNull CSVReader csvReader;

    @BeforeEach
    void setUp() throws IOException {
        //noinspection unchecked
        clientDetailsFuture = mock(CompletableFuture.class);
        when(clientDetailsFuture.isDone()).thenReturn(false);

        csvFile = File.createTempFile("client_details", ".csv");
        clientDetailsQueue = new LinkedBlockingQueue<>();
        clientDetailsCsvWriterTask = new ClientDetailsCsvWriterTask(
                clientDetailsFuture,
                clientDetailsQueue,
                csvFile,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
        csvReader = new CSVReader(new FileReader(csvFile));
    }

    @Test
    void all_client_details_success()
            throws IOException, CsvValidationException, ExecutionException, InterruptedException {
        final ClientDetails clientDetails = new ClientDetails();
        clientDetails.setId("test");
        clientDetails.setConnected(true);
        clientDetails.setSessionExpiryInterval(120L);
        clientDetails.setConnectedAt(OffsetDateTime.parse("2020-07-17T14:36:58.641286+02:00"));
        clientDetails.setMessageQueueSize(50L);
        clientDetails.setWillPresent(true);

        final ClientRestrictions clientRestrictions = new ClientRestrictions();
        clientRestrictions.setMaxMessageSize(256000000L);
        clientRestrictions.setMaxQueueSize(1000L);
        clientRestrictions.setQueuedMessageStrategy("DISCARD");
        clientDetails.setRestrictions(clientRestrictions);

        final ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setSourceIp("127.0.0.1");

        final ProxyInformation proxyInformation = new ProxyInformation();
        proxyInformation.setSourceIp("5.35.166.178");
        proxyInformation.setSourcePort(40101);
        proxyInformation.setDestinationIp("127.125.124.124");
        proxyInformation.setDestinationPort(1883);

        final TLV tlv1 = new TLV();
        final TLV tlv2 = new TLV();
        tlv1.setKey("key1");
        tlv1.setValue("value1");
        tlv2.setKey("key2");
        tlv2.setValue("value1");

        final List<TLV> tlvs = Arrays.asList(tlv1, tlv2);

        proxyInformation.setTlvs(tlvs);
        connectionDetails.setProxyInformation(proxyInformation);
        connectionDetails.setMqttVersion("MQTT 3.1.1");
        connectionDetails.setConnectedListenerId("tcp-listener-1883");
        connectionDetails.setConnectedNodeId("06cnn");
        connectionDetails.setKeepAlive(120);
        connectionDetails.setUsername("user-1");
        connectionDetails.setPassword("pass-1".getBytes());
        connectionDetails.setCleanStart(true);

        final TlsInformation tlsInformation = new TlsInformation();
        tlsInformation.setCipherSuite("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
        tlsInformation.setTlsVersion("TLSv1.2");

        final CertificateInformation certificateInformation = new CertificateInformation();
        certificateInformation.setCommonName("CertCN");
        certificateInformation.setOrganization("my-org");
        certificateInformation.setOrganizationalUnit("org-unit");
        certificateInformation.setSerial("132132312321");
        certificateInformation.setValidFrom(OffsetDateTime.parse("2020-07-17T14:36:58.641286+02:00"));
        certificateInformation.setValidUntil(OffsetDateTime.parse("2020-07-17T14:36:58.641286+02:00"));
        certificateInformation.setCountry("DE");
        certificateInformation.setState("BY");
        tlsInformation.setCertificateInformation(certificateInformation);

        connectionDetails.setTlsInformation(tlsInformation);

        clientDetails.setConnection(connectionDetails);

        clientDetailsQueue.add(clientDetails);

        final CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(clientDetailsCsvWriterTask);

        when(clientDetailsFuture.isDone()).thenReturn(true);

        completableFuture.get();

        assertArrayEquals(EXPORT_CSV_HEADER, csvReader.readNext());

        final String[] expectedRow = {
                "test", "true", "120", "2020-07-17T14:36:58.641286+02:00", "50", "true", "256000000", "1000", "DISCARD",
                "127.0.0.1", "5.35.166.178", "40101", "127.125.124.124", "1883", "key1=value1;key2=value1;",
                "MQTT 3.1.1", "tcp-listener-1883", "06cnn", "120", "user-1", "pass-1", "true",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "TLSv1.2", "CertCN", "my-org", "org-unit", "132132312321",
                "2020-07-17T14:36:58.641286+02:00", "2020-07-17T14:36:58.641286+02:00", "DE", "BY"
        };

        assertArrayEquals(expectedRow, csvReader.readNext());
    }

    @Test
    void all_client_details_no_restrictions()
            throws IOException, CsvValidationException, ExecutionException, InterruptedException {
        final ClientDetails clientDetails = new ClientDetails();
        clientDetails.setId("test");
        clientDetails.setConnected(true);
        clientDetails.setSessionExpiryInterval(120L);
        clientDetails.setConnectedAt(OffsetDateTime.parse("2020-07-17T14:36:58.641286+02:00"));
        clientDetails.setMessageQueueSize(50L);
        clientDetails.setWillPresent(true);
        clientDetails.setRestrictions(null);

        final ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setSourceIp("127.0.0.1");

        final ProxyInformation proxyInformation = new ProxyInformation();
        proxyInformation.setSourceIp("5.35.166.178");
        proxyInformation.setSourcePort(40101);
        proxyInformation.setDestinationIp("127.125.124.124");
        proxyInformation.setDestinationPort(1883);

        final TLV tlv1 = new TLV();
        final TLV tlv2 = new TLV();
        tlv1.setKey("key1");
        tlv1.setValue("value1");
        tlv2.setKey("key2");
        tlv2.setValue("value1");

        final List<TLV> tlvs = Arrays.asList(tlv1, tlv2);

        proxyInformation.setTlvs(tlvs);
        connectionDetails.setProxyInformation(proxyInformation);
        connectionDetails.setMqttVersion("MQTT 3.1.1");
        connectionDetails.setConnectedListenerId("tcp-listener-1883");
        connectionDetails.setConnectedNodeId("06cnn");
        connectionDetails.setKeepAlive(120);
        connectionDetails.setUsername("user-1");
        connectionDetails.setPassword("pass-1".getBytes());
        connectionDetails.setCleanStart(true);

        final TlsInformation tlsInformation = new TlsInformation();
        tlsInformation.setCipherSuite("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
        tlsInformation.setTlsVersion("TLSv1.2");

        final CertificateInformation certificateInformation = new CertificateInformation();
        certificateInformation.setCommonName("CertCN");
        certificateInformation.setOrganization("my-org");
        certificateInformation.setOrganizationalUnit("org-unit");
        certificateInformation.setSerial("132132312321");
        certificateInformation.setValidFrom(OffsetDateTime.parse("2020-07-17T14:36:58.641286+02:00"));
        certificateInformation.setValidUntil(OffsetDateTime.parse("2020-07-17T14:36:58.641286+02:00"));
        certificateInformation.setCountry("DE");
        certificateInformation.setState("BY");
        tlsInformation.setCertificateInformation(certificateInformation);

        connectionDetails.setTlsInformation(tlsInformation);

        clientDetails.setConnection(connectionDetails);

        clientDetailsQueue.add(clientDetails);

        final CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(clientDetailsCsvWriterTask);

        when(clientDetailsFuture.isDone()).thenReturn(true);

        completableFuture.get();

        assertArrayEquals(EXPORT_CSV_HEADER, csvReader.readNext());

        final String[] expectedRow = {
                "test", "true", "120", "2020-07-17T14:36:58.641286+02:00", "50", "true", "", "", "", "127.0.0.1",
                "5.35.166.178", "40101", "127.125.124.124", "1883", "key1=value1;key2=value1;", "MQTT 3.1.1",
                "tcp-listener-1883", "06cnn", "120", "user-1", "pass-1", "true",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "TLSv1.2", "CertCN", "my-org", "org-unit", "132132312321",
                "2020-07-17T14:36:58.641286+02:00", "2020-07-17T14:36:58.641286+02:00", "DE", "BY"
        };

        assertArrayEquals(expectedRow, csvReader.readNext());
    }

    @Test
    void all_client_details_no_connection_details()
            throws IOException, CsvValidationException, ExecutionException, InterruptedException {
        final ClientDetails clientDetails = new ClientDetails();
        clientDetails.setId("test");
        clientDetails.setConnected(true);
        clientDetails.setSessionExpiryInterval(120L);
        clientDetails.setConnectedAt(OffsetDateTime.parse("2020-07-17T14:36:58.641286+02:00"));
        clientDetails.setMessageQueueSize(50L);
        clientDetails.setWillPresent(true);
        clientDetails.setRestrictions(null);
        clientDetails.setConnection(null);

        clientDetailsQueue.add(clientDetails);

        final CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(clientDetailsCsvWriterTask);

        when(clientDetailsFuture.isDone()).thenReturn(true);

        completableFuture.get();

        assertArrayEquals(EXPORT_CSV_HEADER, csvReader.readNext());

        final String[] expectedRow = {
                "test", "true", "120", "2020-07-17T14:36:58.641286+02:00", "50", "true", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""
        };

        assertArrayEquals(expectedRow, csvReader.readNext());
    }

    @Test
    void all_client_details_50_times_success()
            throws IOException, CsvValidationException, InterruptedException, ExecutionException {
        final ClientDetails clientDetails = new ClientDetails();
        clientDetails.setId("test");
        clientDetails.setConnected(true);
        clientDetails.setSessionExpiryInterval(120L);
        clientDetails.setConnectedAt(OffsetDateTime.parse("2020-07-17T14:36:58.641286+02:00"));
        clientDetails.setMessageQueueSize(50L);
        clientDetails.setWillPresent(true);

        final ClientRestrictions clientRestrictions = new ClientRestrictions();
        clientRestrictions.setMaxMessageSize(256000000L);
        clientRestrictions.setMaxQueueSize(1000L);
        clientRestrictions.setQueuedMessageStrategy("DISCARD");
        clientDetails.setRestrictions(clientRestrictions);

        final ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setSourceIp("127.0.0.1");

        final ProxyInformation proxyInformation = new ProxyInformation();
        proxyInformation.setSourceIp("5.35.166.178");
        proxyInformation.setSourcePort(40101);
        proxyInformation.setDestinationIp("127.125.124.124");
        proxyInformation.setDestinationPort(1883);

        final TLV tlv1 = new TLV();
        final TLV tlv2 = new TLV();
        tlv1.setKey("key1");
        tlv1.setValue("value1");
        tlv2.setKey("key2");
        tlv2.setValue("value1");

        final List<TLV> tlvs = Arrays.asList(tlv1, tlv2);

        proxyInformation.setTlvs(tlvs);
        connectionDetails.setProxyInformation(proxyInformation);
        connectionDetails.setMqttVersion("MQTT 3.1.1");
        connectionDetails.setConnectedListenerId("tcp-listener-1883");
        connectionDetails.setConnectedNodeId("06cnn");
        connectionDetails.setKeepAlive(120);
        connectionDetails.setUsername("user-1");
        connectionDetails.setPassword("pass-1".getBytes());
        connectionDetails.setCleanStart(true);

        final TlsInformation tlsInformation = new TlsInformation();
        tlsInformation.setCipherSuite("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
        tlsInformation.setTlsVersion("TLSv1.2");

        final CertificateInformation certificateInformation = new CertificateInformation();
        certificateInformation.setCommonName("CertCN");
        certificateInformation.setOrganization("my-org");
        certificateInformation.setOrganizationalUnit("org-unit");
        certificateInformation.setSerial("132132312321");
        certificateInformation.setValidFrom(OffsetDateTime.parse("2020-07-17T14:36:58.641286+02:00"));
        certificateInformation.setValidUntil(OffsetDateTime.parse("2020-07-17T14:36:58.641286+02:00"));
        certificateInformation.setCountry("DE");
        certificateInformation.setState("BY");
        tlsInformation.setCertificateInformation(certificateInformation);

        connectionDetails.setTlsInformation(tlsInformation);

        clientDetails.setConnection(connectionDetails);

        for (int i = 0; i < 25; i++) {
            clientDetailsQueue.add(clientDetails);
        }

        final CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(clientDetailsCsvWriterTask);

        Thread.sleep(1000);
        for (int i = 0; i < 25; i++) {
            clientDetailsQueue.add(clientDetails);
        }

        when(clientDetailsFuture.isDone()).thenReturn(true);

        completableFuture.get();

        assertArrayEquals(EXPORT_CSV_HEADER, csvReader.readNext());

        final String[] expectedRow = {
                "test", "true", "120", "2020-07-17T14:36:58.641286+02:00", "50", "true", "256000000", "1000", "DISCARD",
                "127.0.0.1", "5.35.166.178", "40101", "127.125.124.124", "1883", "key1=value1;key2=value1;",
                "MQTT 3.1.1", "tcp-listener-1883", "06cnn", "120", "user-1", "pass-1", "true",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "TLSv1.2", "CertCN", "my-org", "org-unit", "132132312321",
                "2020-07-17T14:36:58.641286+02:00", "2020-07-17T14:36:58.641286+02:00", "DE", "BY"
        };
        for (int i = 0; i < 50; i++) {
            assertArrayEquals(expectedRow, csvReader.readNext());
        }
    }

    @Test
    void wait_for_client_details() throws IOException, CsvException {
        final ClientDetails allClientDetails = TestClientDetails.getAllClientDetails();
        clientDetailsQueue = new LinkedBlockingQueue<>(1);
        clientDetailsCsvWriterTask = new ClientDetailsCsvWriterTask(
                clientDetailsFuture,
                clientDetailsQueue,
                csvFile,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        final CompletableFuture<Void> detailsProducerFuture = CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < 50; i++) {
                    Thread.sleep(10);
                    clientDetailsQueue.put(allClientDetails);
                }
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
            when(clientDetailsFuture.isDone()).thenReturn(true);
        });

        final CompletableFuture<Void> clientDetailsCsvWriterFuture =
                CompletableFuture.runAsync(clientDetailsCsvWriterTask);

        detailsProducerFuture.join();
        clientDetailsCsvWriterFuture.join();

        final int writtenCsvLines = csvReader.readAll().size();

        assertEquals(51, writtenCsvLines);
    }
}