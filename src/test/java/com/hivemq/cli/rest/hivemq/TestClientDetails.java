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

package com.hivemq.cli.rest.hivemq;

import com.hivemq.cli.openapi.hivemq.HivemqOpenapiCertificateInformation;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiClientDetails;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiClientRestrictions;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiConnectionDetails;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiProxyInformation;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiTLV;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiTlsInformation;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

public class TestClientDetails {

    public static HivemqOpenapiClientDetails getAllClientDetails() {
        final HivemqOpenapiClientDetails clientDetails = new HivemqOpenapiClientDetails();
        clientDetails.setId("test");
        clientDetails.setConnected(true);
        clientDetails.setSessionExpiryInterval(120L);
        clientDetails.setConnectedAt(OffsetDateTime.parse("2020-07-17T14:36:58.641286+02:00"));
        clientDetails.setMessageQueueSize(50L);
        clientDetails.setWillPresent(true);

        final HivemqOpenapiClientRestrictions clientRestrictions = new HivemqOpenapiClientRestrictions();
        clientRestrictions.setMaxMessageSize(256000000L);
        clientRestrictions.setMaxQueueSize(1000L);
        clientRestrictions.setQueuedMessageStrategy("DISCARD");
        clientDetails.setRestrictions(clientRestrictions);

        final HivemqOpenapiConnectionDetails connectionDetails = new HivemqOpenapiConnectionDetails();
        connectionDetails.setSourceIp("127.0.0.1");

        final HivemqOpenapiProxyInformation proxyInformation = new HivemqOpenapiProxyInformation();
        proxyInformation.setSourceIp("5.35.166.178");
        proxyInformation.setSourcePort(40101);
        proxyInformation.setDestinationIp("127.125.124.124");
        proxyInformation.setDestinationPort(1883);

        final HivemqOpenapiTLV tlv1 = new HivemqOpenapiTLV();
        final HivemqOpenapiTLV tlv2 = new HivemqOpenapiTLV();
        tlv1.setKey("key1");
        tlv1.setValue("value1");
        tlv2.setKey("key2");
        tlv2.setValue("value1");

        final List<HivemqOpenapiTLV> tlvs = Arrays.asList(tlv1, tlv2);

        proxyInformation.setTlvs(tlvs);
        connectionDetails.setProxyInformation(proxyInformation);


        connectionDetails.setMqttVersion("MQTT 3.1.1");
        connectionDetails.setConnectedListenerId("tcp-listener-1883");
        connectionDetails.setConnectedNodeId("06cnn");
        connectionDetails.setKeepAlive(120);
        connectionDetails.setUsername("user-1");
        connectionDetails.setPassword("pass-1".getBytes());
        connectionDetails.setCleanStart(true);

        final HivemqOpenapiTlsInformation tlsInformation = new HivemqOpenapiTlsInformation();
        tlsInformation.setCipherSuite("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
        tlsInformation.setTlsVersion("TLSv1.2");

        final HivemqOpenapiCertificateInformation certificateInformation = new HivemqOpenapiCertificateInformation();
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

        return clientDetails;
    }
}
