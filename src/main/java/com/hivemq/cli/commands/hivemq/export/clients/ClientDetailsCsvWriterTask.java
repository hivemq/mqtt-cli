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
package com.hivemq.cli.commands.hivemq.export.clients;

import com.opencsv.CSVWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openapitools.client.model.CertificateInformation;
import org.openapitools.client.model.ClientDetails;
import org.openapitools.client.model.ClientRestrictions;
import org.openapitools.client.model.ConnectionDetails;
import org.openapitools.client.model.ProxyInformation;
import org.openapitools.client.model.TLV;
import org.openapitools.client.model.TlsInformation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;

public class ClientDetailsCsvWriterTask implements Callable<Void> {

    public static final String[] EXPORT_CSV_HEADER = {
            "clientId",
            "connected",
            "sessionExpiryInterval",
            "connectedAt",
            "messageQueueSize",
            "willPresent",
            "maxMessageSize",
            "maxQueueSize",
            "queuedMessageStrategy",
            "ip",
            "sourceIp",
            "sourcePort",
            "destinationIp",
            "destinationPort",
            "tlvs",
            "mqttVersion",
            "connectedListenerId",
            "connectedNodeId",
            "keepAlive",
            "username",
            "password",
            "cleanStart",
            "cipherSuite",
            "tlsVersion",
            "certificateCommonName",
            "certificateOrganization",
            "certificateOrganizationalUnit",
            "certificateSerial",
            "certificateValidFrom",
            "certificateValidUntil",
            "certificateCountry",
            "certificateState"
    };

    final @NotNull Future<Void> clientDetailsFuture;
    final @NotNull Queue<ClientDetails> clientDetailsQueue;
    final @NotNull File file;
    final @NotNull CSVWriter csvWriter;

    long writtenClientDetails = 0;

    public ClientDetailsCsvWriterTask(final @NotNull Future<Void> clientDetailsFuture,
                                      final @NotNull Queue<ClientDetails> clientDetailsQueue,
                                      final @NotNull File file,
                                      final char lineSeparator,
                                      final char quoteCharacter,
                                      final char escapeCharacter,
                                      final @NotNull String lineEndCharacter) throws IOException {
        this.clientDetailsFuture = clientDetailsFuture;
        this.clientDetailsQueue = clientDetailsQueue;
        this.file = file;
        csvWriter = new CSVWriter(
                new FileWriter(file, false),
                lineSeparator,
                quoteCharacter,
                escapeCharacter,
                lineEndCharacter
        );
    }

    @Override
    public Void call() throws IOException, InterruptedException {

        writeHeader();

        while (!clientDetailsFuture.isDone() || !clientDetailsQueue.isEmpty()) {

            while (!clientDetailsQueue.isEmpty()) {
                final ClientDetails clientDetails = clientDetailsQueue.poll();
                writeRow(clientDetails);
                writtenClientDetails += 1;
            }

            csvWriter.flush();

            Thread.sleep(50);
        }
        return null;
    }

    public long getWrittenClientDetails() { return writtenClientDetails; }

    private void writeHeader() {
        csvWriter.writeNext(EXPORT_CSV_HEADER);
    }

    private void writeRow(final @NotNull ClientDetails clientDetails) {
        final List<String> row = new ArrayList<>();
        row.add(clientDetails.getId());
        row.add(toCsvString(clientDetails.getConnected()));
        row.add(toCsvString(clientDetails.getSessionExpiryInterval()));
        row.add(toCsvString(clientDetails.getConnectedAt()));
        row.add(toCsvString(clientDetails.getMessageQueueSize()));
        row.add(toCsvString(clientDetails.getWillPresent()));

        final ClientRestrictions restrictions = clientDetails.getRestrictions();
        addRestrictions(row, restrictions);

        final ConnectionDetails connectionDetails = clientDetails.getConnection();
        addConnectionDetails(row, connectionDetails);

        csvWriter.writeNext(row.toArray(new String[]{}));
    }

    private void addConnectionDetails(List<String> row, ConnectionDetails connectionDetails) {
        if (connectionDetails != null) {

            row.add(connectionDetails.getSourceIp());

            final ProxyInformation proxyInformation = connectionDetails.getProxyInformation();
            addProxyInformation(row, proxyInformation);

            row.add(connectionDetails.getMqttVersion());
            row.add(connectionDetails.getConnectedListenerId());
            row.add(connectionDetails.getConnectedNodeId());
            row.add(toCsvString(connectionDetails.getKeepAlive()));
            row.add(connectionDetails.getUsername());

            final byte[] password = connectionDetails.getPassword();
            if (password != null) {
                row.add(new String(password, StandardCharsets.UTF_8));
            } else {
                row.add(null);
            }

            row.add(toCsvString(connectionDetails.getCleanStart()));

            final TlsInformation tlsInformation = connectionDetails.getTlsInformation();
            addTlsInformation(row, tlsInformation);
        }
        else {
            row.add(null);
            addProxyInformation(row, null);
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            addTlsInformation(row, null);
        }
    }

    private void addTlsInformation(List<String> row, TlsInformation tlsInformation) {
        if (tlsInformation != null) {

            row.add(tlsInformation.getCipherSuite());
            row.add(tlsInformation.getTlsVersion());

            final CertificateInformation certificateInformation = tlsInformation.getCertificateInformation();
            addCertificateInformation(row, certificateInformation);
        }
        else {
            row.add(null);
            row.add(null);
            addCertificateInformation(row, null);
        }
    }

    private void addCertificateInformation(List<String> row, CertificateInformation certificateInformation) {
        if (certificateInformation != null) {
            row.add(certificateInformation.getCommonName());
            row.add(certificateInformation.getOrganization());
            row.add(certificateInformation.getOrganizationalUnit());
            row.add(certificateInformation.getSerial());
            row.add(toCsvString(certificateInformation.getValidFrom()));
            row.add(toCsvString(certificateInformation.getValidUntil()));
            row.add(toCsvString(certificateInformation.getCountry()));
            row.add(toCsvString(certificateInformation.getState()));
        }
        else {
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
        }
    }

    private void addProxyInformation(List<String> row, ProxyInformation proxyInformation) {
        if (proxyInformation != null) {
            row.add(proxyInformation.getSourceIp());
            row.add(toCsvString(proxyInformation.getSourcePort()));
            row.add(proxyInformation.getDestinationIp());
            row.add(toCsvString(proxyInformation.getDestinationPort()));

            final List<TLV> tlvs = proxyInformation.getTlvs();
            if (tlvs != null) {
                final StringBuilder sb = new StringBuilder();
                    for (TLV tlv : tlvs) {
                        sb.append(tlv.getKey()).append("=");
                        final String value = tlv.getValue();
                        if (value != null) {
                            sb.append(value);
                        }
                        sb.append(';');
                    }
                row.add(sb.toString());
            } else {
                row.add(null);
            }
        }
        else {
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
            row.add(null);
        }
    }

    private void addRestrictions(List<String> row, ClientRestrictions restrictions) {
        if (restrictions != null) {
            row.add(toCsvString(restrictions.getMaxMessageSize()));
            row.add(toCsvString(restrictions.getMaxQueueSize()));
            row.add(toCsvString(restrictions.getQueuedMessageStrategy()));
        } else {
            row.add(null);
            row.add(null);
            row.add(null);
        }
    }

    private String toCsvString(final @Nullable Object object) {
        return object != null ? object.toString() : null;
    }

}
