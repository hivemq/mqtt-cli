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

    public static final @NotNull String CLIENT_IDS_WITH_CURSOR = """
            {
              "items": [
                {
                  "id": "client-1"
                },
                {
                  "id": "client-2"
                },
                {
                  "id": "client-3"
                },
                {
                  "id": "client-4"
                },
                {
                  "id": "client-5"
                },
                {
                  "id": "client-6"
                },
                {
                  "id": "client-7"
                },
                {
                  "id": "client-8"
                },
                {
                  "id": "client-9"
                },
                {
                  "id": "client-10"
                }
              ],
              "_links": {
                "next": "/api/v1/hivemq/clients?cursor=bmV4dHJlc3VsdGFzZGprYXNkamFzbGRqYXM_"
              }
            }""";

    public static final @NotNull String CLIENT_IDS_SINGLE_RESULT = """
            {
              "items": [
                {
                  "id": "client-π"
                }
              ]
            }""";

    public static final @NotNull String CLIENT_IDS_EMPTY = """
            {
              "items": []
            }""";

    public static final @NotNull String CLIENT_IDS_INVALID_CURSOR = """
            {
              "errors": [
                {
                  "title": "Parameter invalid",
                  "detail": "Query parameter cursor is invalid"
                }
              ]
            }""";

    public static final @NotNull String CLIENT_IDS_CURSOR_NOT_VALID_ANYMORE = """
            {
              "errors": [
                {
                  "title": "Cursor not valid anymore",
                  "detail": "The passed cursor is not valid anymore. You can request this resource without a cursor to start from the beginning"
                }
              ]
            }""";

    public static final @NotNull String CLIENT_IDS_REPLICATION = """
            {
              "errors": [
                {
                  "title": "The endpoint is temporarily not available",
                  "detail": "The endpoint is temporarily not available. Please try again later"
                }
              ]
            }""";

    public static final @NotNull String CLIENT_DETAILS_ALL = """
            {
              "client": {
                "id": "client-1",
                "connected": true,
                "sessionExpiryInterval": 120,
                "connectedAt": "2019-10-10T16:43:14Z",
                "messageQueueSize": 0,
                "willPresent": true,
                "restrictions": {
                  "maxMessageSize": 256000000,
                  "maxQueueSize": 1000,
                  "queuedMessageStrategy": "DISCARD"
                },
                "connection": {
                  "sourceIp": "127.0.0.1",
                  "proxyInformation": {
                    "sourceIp": "5.35.166.178",
                    "sourcePort": 40101,
                    "destinationIp": "172.31.24.178",
                    "destinationPort": 1883,
                    "tlvs": [
                      {
                        "key": "key1",
                        "value": "value1"
                      },
                      {
                        "key": "key2",
                        "value": "value2"
                      }
                    ]
                  },
                  "mqttVersion": "3.1.1",
                  "connectedListenerId": "tcp-listener-1883",
                  "connectedNodeId": "O6ccn",
                  "keepAlive": "120",
                  "username": "user-1",
                  "password": "pass-1",\s
                  "cleanStart": true,
                  "tlsInformation": {
                    "cipherSuite": "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                    "tlsVersion": "TLSv1.2",
                    "certificateInformation": {
                      "commonName": "CertCN",
                      "organization": "my-org",
                      "organizationalUnit": "org-unit",
                      "serial": "132132312321",
                      "validFrom": "2019-01-01T10:20:30Z",
                      "validUntil": "2019-12-01T10:20:30Z",
                      "country": "DE",
                      "state": "BY"
                    }
                  }
                }
              }
            }""";

    public static final @NotNull String CLIENT_DETAILS_CONNECTED = """
            {
              "client": {
                "id": "client-1",
                "connected": true,
                "sessionExpiryInterval": 120,
                "connectedAt": "2019-10-10T16:43:14Z",
                "messageQueueSize": 0,
                "willPresent": true,
                "restrictions": {
                  "maxMessageSize": 256000000,
                  "maxQueueSize": 1000,
                  "queuedMessageStrategy": "DISCARD"
                },
                "connection": {
                  "sourceIp": "127.0.0.1",
                  "mqttVersion": "3.1.1",
                  "connectedListenerId": "tcp-listener-1883",
                  "connectedNodeId": "O6ccn",
                  "keepAlive": "120",
                  "cleanStart": true
                }
              }
            }""";

    public static final @NotNull String CLIENT_DETAILS_PERSISTENT_OFFLINE = """
            {
              "client": {
                "id": "client-1",
                "connected": false,
                "sessionExpiryInterval": 120,
                "messageQueueSize": 674,
                "willPresent": false,
                "restrictions": {
                  "maxQueueSize": 1000,
                  "queuedMessageStrategy": "DISCARD"
                }
              }
            }""";
}
