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

import com.hivemq.cli.openapi.hivemq.CertificateInformation;
import com.hivemq.cli.openapi.hivemq.ClientDetails;
import com.hivemq.cli.openapi.hivemq.ClientRestrictions;
import com.hivemq.cli.openapi.hivemq.ConnectionDetails;
import com.hivemq.cli.openapi.hivemq.ProxyInformation;
import com.hivemq.cli.openapi.hivemq.TLV;
import com.hivemq.cli.openapi.hivemq.TlsInformation;
import com.opencsv.CSVWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ClientDetailsCsvWriterTask implements Runnable {

    static final @NotNull String @NotNull [] EXPORT_CSV_HEADER = {
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
            "certificateState"};

    private final @NotNull AtomicLong writtenClientDetails = new AtomicLong(0);
    private final @NotNull CompletableFuture<Void> clientDetailsFuture;
    private final @NotNull BlockingQueue<ClientDetails> clientDetailsQueue;
    private final @NotNull File file;
    private final @NotNull CSVWriter csvWriter;
    private final @NotNull BufferedWriter bufferedFileWriter;

    public ClientDetailsCsvWriterTask(
            final @NotNull CompletableFuture<Void> clientDetailsFuture,
            final @NotNull BlockingQueue<ClientDetails> clientDetailsQueue,
            final @NotNull File file,
            final char lineSeparator,
            final char quoteCharacter,
            final char escapeCharacter,
            final @NotNull String lineEndCharacter) throws IOException {
        this.clientDetailsFuture = clientDetailsFuture;
        this.clientDetailsQueue = clientDetailsQueue;
        this.file = file;
        this.bufferedFileWriter = new BufferedWriter(new FileWriter(file, false));

        csvWriter = new CSVWriter(bufferedFileWriter, lineSeparator, quoteCharacter, escapeCharacter, lineEndCharacter);
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                bufferedFileWriter.close();
            } catch (final IOException e) {
                Logger.error(e, "Interrupted before CSV could be written - CSV may be malformed");
                System.err.println("Interrupted before all content was written, output file may be incomplete");
            }
        }));

        try {
            writeHeader();

            while (!clientDetailsFuture.isDone() || !clientDetailsQueue.isEmpty()) {

                final ClientDetails clientDetails = clientDetailsQueue.poll(50, TimeUnit.MILLISECONDS);

                if (clientDetails != null) {
                    writeRow(clientDetails);
                    writtenClientDetails.incrementAndGet();
                }
            }
            csvWriter.close();
        } catch (final Exception e) {
            Logger.error(e, "Writing of CSV file failed");
            throw new CompletionException(e);
        }
        Logger.debug("Finished writing {} client details to CSV file {}", writtenClientDetails, file.getAbsolutePath());
    }

    public long getWrittenClientDetails() {
        return writtenClientDetails.get();
    }

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

    private void addConnectionDetails(
            final @NotNull List<String> row, final @Nullable ConnectionDetails connectionDetails) {
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
                row.add(null); // password
            }

            row.add(toCsvString(connectionDetails.getCleanStart()));

            final TlsInformation tlsInformation = connectionDetails.getTlsInformation();
            addTlsInformation(row, tlsInformation);
        } else {
            row.add(null); // Ip
            addProxyInformation(row, null);
            row.add(null); // mqttVersion
            row.add(null); // connectedListenerId
            row.add(null); // connectedNodeId
            row.add(null); // keepAlive
            row.add(null); // username
            row.add(null); // password
            row.add(null); // cleanStart
            addTlsInformation(row, null);
        }
    }

    private void addTlsInformation(final @NotNull List<String> row, final @Nullable TlsInformation tlsInformation) {
        if (tlsInformation != null) {
            row.add(tlsInformation.getCipherSuite());
            row.add(tlsInformation.getTlsVersion());

            final CertificateInformation certificateInformation = tlsInformation.getCertificateInformation();
            addCertificateInformation(row, certificateInformation);
        } else {
            row.add(null); // cipherSuite
            row.add(null); // tlsVersion
            addCertificateInformation(row, null);
        }
    }

    private void addCertificateInformation(
            final @NotNull List<String> row, final @Nullable CertificateInformation certificateInformation) {
        if (certificateInformation != null) {
            row.add(certificateInformation.getCommonName());
            row.add(certificateInformation.getOrganization());
            row.add(certificateInformation.getOrganizationalUnit());
            row.add(certificateInformation.getSerial());
            row.add(toCsvString(certificateInformation.getValidFrom()));
            row.add(toCsvString(certificateInformation.getValidUntil()));
            row.add(toCsvString(certificateInformation.getCountry()));
            row.add(toCsvString(certificateInformation.getState()));
        } else {
            row.add(null); // certificateCommonName
            row.add(null); // certificateOrganization
            row.add(null); // certificateOrganizationalUnit
            row.add(null); // certificateSerial
            row.add(null); // certificateValidFrom
            row.add(null); // certificateValidUntil
            row.add(null); // certificateCountry
            row.add(null); // certificateState
        }
    }

    private void addProxyInformation(
            final @NotNull List<String> row, final @Nullable ProxyInformation proxyInformation) {
        if (proxyInformation != null) {
            row.add(proxyInformation.getSourceIp());
            row.add(toCsvString(proxyInformation.getSourcePort()));
            row.add(proxyInformation.getDestinationIp());
            row.add(toCsvString(proxyInformation.getDestinationPort()));

            final List<TLV> tlvs = proxyInformation.getTlvs();
            if (tlvs != null) {
                final StringBuilder sb = new StringBuilder();
                for (final TLV tlv : tlvs) {
                    sb.append(tlv.getKey()).append("=");
                    final String value = tlv.getValue();
                    if (value != null) {
                        sb.append(value);
                    }
                    sb.append(';');
                }
                row.add(sb.toString());
            } else {
                row.add(null); // tlvs
            }
        } else {
            row.add(null); // sourceIp
            row.add(null); // sourcePort
            row.add(null); // destinationIp
            row.add(null); // destinationPort
            row.add(null); // tlvs
        }
    }

    private void addRestrictions(final @NotNull List<String> row, final @Nullable ClientRestrictions restrictions) {
        if (restrictions != null) {
            row.add(toCsvString(restrictions.getMaxMessageSize()));
            row.add(toCsvString(restrictions.getMaxQueueSize()));
            row.add(toCsvString(restrictions.getQueuedMessageStrategy()));
        } else {
            row.add(null); // maxMessageSize
            row.add(null); // maxQueueSize
            row.add(null); // queuedMessageStrategy
        }
    }

    private @Nullable String toCsvString(final @Nullable Object object) {
        return object != null ? object.toString() : null;
    }
}
