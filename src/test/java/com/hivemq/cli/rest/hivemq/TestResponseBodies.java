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

import org.jetbrains.annotations.NotNull;

public class TestResponseBodies {

    public static final @NotNull String CLIENT_IDS_WITH_CURSOR =
            //@formatter:off
            "{\n" +
                    "  \"items\": [\n" +
                    "    {\n" +
                    "      \"id\": \"client-1\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": \"client-2\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": \"client-3\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": \"client-4\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": \"client-5\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": \"client-6\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": \"client-7\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": \"client-8\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": \"client-9\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": \"client-10\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"_links\": {\n" +
                    "    \"next\": \"/api/v1/hivemq/clients?cursor=bmV4dHJlc3VsdGFzZGprYXNkamFzbGRqYXM_\"\n" +
                    "  }\n" +
                    "}";
            //@formatter:on

    public static final @NotNull String CLIENT_IDS_SINGLE_RESULT =
            //@formatter:off
            "{\n" +
                    "  \"items\": [\n" +
                    "    {\n" +
                    "      \"id\": \"client-π\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            //@formatter:on

    public static final @NotNull String CLIENT_IDS_EMPTY =
            //@formatter:off
            "{\n" +
                    "  \"items\": []\n" +
                    "}";
            //@formatter:on

    public static final @NotNull String CLIENT_IDS_INVALID_CURSOR =
            //@formatter:off
            "{\n" +
                    "  \"errors\": [\n" +
                    "    {\n" +
                    "      \"title\": \"Parameter invalid\",\n" +
                    "      \"detail\": \"Query parameter cursor is invalid\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            //@formatter:on

    public static final @NotNull String CLIENT_IDS_CURSOR_NOT_VALID_ANYMORE =
            //@formatter:off
            "{\n" +
                    "  \"errors\": [\n" +
                    "    {\n" +
                    "      \"title\": \"Cursor not valid anymore\",\n" +
                    "      \"detail\": \"The passed cursor is not valid anymore. You can request this resource without a cursor to start from the beginning\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            //@formatter:on

    public static final @NotNull String CLIENT_IDS_REPLICATION =
            //@formatter:off
            "{\n" +
                    "  \"errors\": [\n" +
                    "    {\n" +
                    "      \"title\": \"The endpoint is temporarily not available\",\n" +
                    "      \"detail\": \"The endpoint is temporarily not available. Please try again later\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            //@formatter:on

    public static final @NotNull String CLIENT_DETAILS_ALL =
            //@formatter:off
            "{\n" +
                    "  \"client\": {\n" +
                    "    \"id\": \"client-1\",\n" +
                    "    \"connected\": true,\n" +
                    "    \"sessionExpiryInterval\": 120,\n" +
                    "    \"connectedAt\": \"2019-10-10T16:43:14Z\",\n" +
                    "    \"messageQueueSize\": 0,\n" +
                    "    \"willPresent\": true,\n" +
                    "    \"restrictions\": {\n" +
                    "      \"maxMessageSize\": 256000000,\n" +
                    "      \"maxQueueSize\": 1000,\n" +
                    "      \"queuedMessageStrategy\": \"DISCARD\"\n" +
                    "    },\n" +
                    "    \"connection\": {\n" +
                    "      \"sourceIp\": \"127.0.0.1\",\n" +
                    "      \"proxyInformation\": {\n" +
                    "        \"sourceIp\": \"5.35.166.178\",\n" +
                    "        \"sourcePort\": 40101,\n" +
                    "        \"destinationIp\": \"172.31.24.178\",\n" +
                    "        \"destinationPort\": 1883,\n" +
                    "        \"tlvs\": [\n" +
                    "          {\n" +
                    "            \"key\": \"key1\",\n" +
                    "            \"value\": \"value1\"\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"key\": \"key2\",\n" +
                    "            \"value\": \"value2\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      },\n" +
                    "      \"mqttVersion\": \"3.1.1\",\n" +
                    "      \"connectedListenerId\": \"tcp-listener-1883\",\n" +
                    "      \"connectedNodeId\": \"O6ccn\",\n" +
                    "      \"keepAlive\": \"120\",\n" +
                    "      \"username\": \"user-1\",\n" +
                    "      \"password\": \"pass-1\", \n" +
                    "      \"cleanStart\": true,\n" +
                    "      \"tls\": {\n" +
                    "        \"cipherSuite\": \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                    "        \"tlsVersion\": \"TLSv1.2\",\n" +
                    "        \"certificate\": {\n" +
                    "          \"commonName\": \"CertCN\",\n" +
                    "          \"organization\": \"my-org\",\n" +
                    "          \"organizationalUnit\": \"org-unit\",\n" +
                    "          \"serial\": \"132132312321\",\n" +
                    "          \"validFrom\": \"2019-01-01T10:20:30Z\",\n" +
                    "          \"validUntil\": \"2019-12-01T10:20:30Z\",\n" +
                    "          \"country\": \"DE\",\n" +
                    "          \"state\": \"BY\"\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            //@formatter:on

    public static final @NotNull String CLIENT_DETAILS_CONNECTED =
            //@formatter:off
            "{\n" +
                    "  \"client\": {\n" +
                    "    \"id\": \"client-1\",\n" +
                    "    \"connected\": true,\n" +
                    "    \"sessionExpiryInterval\": 120,\n" +
                    "    \"connectedAt\": \"2019-10-10T16:43:14Z\",\n" +
                    "    \"messageQueueSize\": 0,\n" +
                    "    \"willPresent\": true,\n" +
                    "    \"restrictions\": {\n" +
                    "      \"maxMessageSize\": 256000000,\n" +
                    "      \"maxQueueSize\": 1000,\n" +
                    "      \"queuedMessageStrategy\": \"DISCARD\"\n" +
                    "    },\n" +
                    "    \"connection\": {\n" +
                    "      \"sourceIp\": \"127.0.0.1\",\n" +
                    "      \"mqttVersion\": \"3.1.1\",\n" +
                    "      \"connectedListenerId\": \"tcp-listener-1883\",\n" +
                    "      \"connectedNodeId\": \"O6ccn\",\n" +
                    "      \"keepAlive\": \"120\",\n" +
                    "      \"cleanStart\": true\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            //@formatter:on

    public static final @NotNull String CLIENT_DETAILS_PERSISTENT_OFFLINE =
            //@formatter:off
            "{\n" +
                    "  \"client\": {\n" +
                    "    \"id\": \"client-1\",\n" +
                    "    \"connected\": false,\n" +
                    "    \"sessionExpiryInterval\": 120,\n" +
                    "    \"sessionExpiresAt\": \"2019-10-10T16:45:14Z\",\n" +
                    "    \"disconnectedAt\": \"2019-10-10T16:43:14Z\",\n" +
                    "    \"messageQueueSize\": 674,\n" +
                    "    \"willPresent\": false,\n" +
                    "    \"restrictions\": {\n" +
                    "      \"maxQueueSize\": 1000,\n" +
                    "      \"queuedMessageStrategy\": \"DISCARD\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            //@formatter:on

    public static final @NotNull String CLIENT_DETAILS_NOT_FOUND =
            //@formatter:off
            "{\n" +
                    "  \"errors\": [\n" +
                    "    {\n" +
                    "      \"title\": \"Requested resource not found\",\n" +
                    "      \"detail\": \"Client with id 'client-1' not found\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            //@formatter:on
}
