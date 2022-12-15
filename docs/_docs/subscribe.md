---
nav_order: 4
redirect_from: /docs/subscribe.html
---

# Subscribe
*** 
Subscribes a client to one or more topics.
If the Subscribe command is not called in Shell-Mode, it will block the console and write the received publishes to the console.

## Simple Examples

 
|Command                                         |Explanation                                                              |
|------------------------------------------------|-------------------------------------------------------------------------|
| ``mqtt sub -t topic`` | Subscribe to a topic with default settings and block the console.
| ``mqtt sub -t test1 -t test2``| Subscribe to the topics 'test1' and 'test2' with default settings and block the console.
| ``mqtt sub -t test -h localhost -p 1884``| Subscribe to topic 'test' at a broker with the address 'localhost:1884'.

<!---
See also 
```
mqtt sub --help
```
--> 

## Synopsis

```
mqtt sub    -t <topics> [-t <topics>]... 
            [-q <qos>]... 
            [-of <receivedMessagesFile>] 
            [-b64]
            [-J]
            [-T]
            [-up <userProperties>]... 
            [-dsvl]
            [-h <host>] 
            [-p <port>]                                         
            [-V <version>] 
            [-i <identifier>] 
            [-ip <identifierPrefix>]
            [-k <keepAlive>]  
            [-se <sessionExpiryInterval>]
            [--[no-]cleanStart]      
            [-Cup <connectUserProperties>]...
            [-ws]
            [-ws:path <webSocketPath>] 
            [-u <user>] 
            [-pw [<password>]]
            [-pw:env [<environmentVariable>]]
            [-pw:file FILE]
            [--cert <clientCertificate>] 
            [--key <clientPrivateKey>]             
            [--cafile FILE]... 
            [--capath DIR]... 
            [--ciphers <cipherSuites>[:<cipherSuites>...]]... 
            [--tls-version <supportedTLSVersions>]... 
            [-Wd <willDelayInterval>] 
            [-We <willMessageExpiryInterval>] 
            [-Wm <willMessage>] 
            [-Wq <willQos>]   
            [-Wr] 
            [-Wt <willTopic>] 
            [-Wcd <willCorrelationData>]
            [-Wct <willContentType>] 
            [-Wpf <willPayloadFormatIndicator>]
            [-Wrt <willResponseTopic>] 
            [-Wup <willUserProperties>]...
            [--rcvMax <receiveMaximum>] 
            [--sendMax <sendMaximum>]
            [--maxPacketSize <maximumPacketSize>] 
            [--sendMaxPacketSize <sendMaximumPacketSize>] 
            [--topicAliasMax <topicAliasMaximum>] 
            [--sendTopicAliasMax <sendTopicAliasMaximum>]                                                                                  
            [--[no-]reqProblemInfo] 
            [--reqResponseInfo] 
            [--help] 
            [--version]
```
***

### Subscribe Options

 
|Option    |Long Version                    | Explanation                                        | Default
|----------|--------------------------------|--------------------------------------------------------------------------------------|--------------|
| ``-t``   | ``--topic``| The MQTT topic the client will subscribe to. |
| ``-q`` | ``--qos`` |  Define the quality of service level. If only one QoS is specified it will be used for all topics.<br> You can define a specific QoS level for every topic. The corresponding QoS levels will be matched in order to the given topics. | ``0``
| ``-of``| ``--outputToFile`` | If a file is given print the received publishes to the specified output file. If the file is not present it will be created. |
| ``-b64``| ``--base64``| Whether the received publish messages will be base64 encoded. | ``false``
| ``-J``  | ``--jsonOutput`` | Print the received publishes in pretty JSON format. | `false`
| ``-T``  | ``--showTopics`` | Prepend the specific topic name to the received publish. | `false`
| ``-up``  | ``--userProperty`` | A user property of the subscribe message. |

***

## Connect Options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-h``   | ``--host``| The MQTT host. | ``localhost``
| ``-p``  | ``--port``| The MQTT port. | ``1883``
| ``-V``   | ``--mqttVersion``| The MQTT version can be set to 3 or 5. | ``MQTT  v.5.0``
| ``-i``   | ``--identifier`` | A unique client identifier can be defined. | A randomly generated UTF-8 String.
| ``-ip``  | ``--identifierPrefix``| The prefix for randomly generated client identifiers, if no identifier given. | ``mqttClient``
|    | ``--[no-]cleanStart`` | Whether the client should start a clean session. | ``true``
| ``k``     | ``--keepAlive``   |   The keep alive of the client (in seconds) | ``60``
| ``-se``  | ``--sessionExpiryInterval`` | Session expiry value in seconds. | ``0`` (Instant Expiry)
| ``-Cup``  | ``--connectUserProperty`` | A user property of the subscribe message. |
| ``--ws``  |  | Use WebSocket transport protocol. | ``false``
| ``--ws:path``  |  | The path to the WebSocket located at given broker host. | 
| ``-l`` | | Log to ~./mqtt.cli/logs (Configurable through ~/.mqtt-cli/config.properties) | ``false``

***

## Security Options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-s``    | ``--secure``  | Whether a default SSL configuration is used. | ``false``
| ``-u``   | ``--user`` | Define the username for authentication. |
| ``-pw``  | ``--password`` | Define the password for authentication directly. <br> If left blank the user will be prompted for the password in console. |
| ``-pw:env``  |  | Define that the password for authentication is read in from an environment variable. | ``MQTT_CLI_PW`` if option is specified without value
| ``-pw:file``  |  | Define the path to a file from which the password is read in. |
|   |   ``--cert``  |   The path to the client certificate to use for client-side authentication. |
|   |   ``--key``   |   The path to the corresponding private key for the given client certificate.    |
|   | ``--cafile``    | The path to the file containing a trusted CA certificate to enable encrypted certificate based communication. |
|   | ``--capath``  | The path to the directory containing trusted CA certificates to enable encrypted certificate based communication. |
|   | ``--ciphers``  | The supported cipher suites in IANA string format concatenated by the ':' character if more than one cipher should be supported. <br> e.g ``TLS_CIPHER_1:TLS_CIPHER_2`` <br> See [IANA Format](https://www.iana.org/assignments/tls-parameters/tls-parameters.xml) for supported cipher suite strings. |
|   |   ``--tls-version``   |   The TLS version to use - ``TLSv1.1`` ``TLSv1.2`` ``TLSv1.3`` | ``TLSv1.2`` |

*** 

## Will Options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-Wd`` | ``--willDelayInterval`` | Will delay interval in seconds. | ``0``
| ``-We``   | ``--willMessageExpiryInterval``   | Lifetime of the will message in seconds. <br> Can be disabled by setting it to ``4_294_967_295``| ``4_294_967_295`` (Disabled)
| ``-Wm``  | ``--willPayload`` | Payload of the will message. |
| ``-Wq``   | ``--willQualityOfService`` | QoS level of the will message. | ``0``
| ``-Wr``   | ``--willRetain``  | Retain the will message. | ``false``
| ``-Wt``  | ``--willTopic`` | Topic of the will message.  |
| ``-Wcd``  | ``--willCorrelationData`` | Correlation data of the will message  |
| ``-Wct``   | ``--willContentType`` |   Description of the will message's content. |
| ``-Wpf``  | ``--willPayloadFormatIndicator`` |Payload format can be explicitly specified as ``UTF8`` else it may be ``UNSPECIFIED``. |
| ``-Wrt``  | ``--willResponseTopic`` | Topic Name for a response message.   |
| ``-Wup``   | ``--willUserProperty``  | A user property of the will message. |

*** 

## Connect Restrictions

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
|   |  ``--rcvMax``  |  The maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts from the server concurrently.  | ``65535``
|   |  ``--sendMax`` |  The maximum amount of not acknowledged publishes with QoS 1 or 2 the client send to the server concurrently.  | ``65535``
|   |  ``--maxPacketSize`` | The maximum packet size the client accepts from the server. | ``268435460``
|   | ``--sendMaxPacketSize`` |  The maximum packet size the client sends to the server. | ``268435460``
|   |  ``--topicAliasMax``  |  The maximum amount of topic aliases the client accepts from the server.  | ``0``
|   |  ``--sendTopicAliasMax``  |  The maximum amount of topic aliases the client sends to the server.  | ``16``
|   |  `` --[no-]reqProblemInfo`` |  The client requests problem information from the server.  | ``true``
|   |  ``--reqResponseInfo``  | The client requests response information from the server. | ``false``

*** 

## Further Examples

> Subscribe to one topic with default QoS ``Exactly Once``

> **NOTE**: If you specify one QoS and multiple topics, the QoS will be used for all topics.

```
$ mqtt sub -t topic1 -t topic2 -q 2  
```

***

> Subscribe to the given topics with a specific QoS for each topic. ``('topic1' will have QoS 0, 'topic2' QoS 1 and 'topic3' QoS 2)``

```
$ mqtt sub -t topic1 -q 0 -t topic2 -q 1 -t topic3 -q 2
```

***

> Subscribe to a topic and output the received publish messages to the file ``publishes.log`` in the current directory

> **NOTE**: If the file is not created yet it will be created by the CLI. If it is present the received publish messages will be appended to the file.

```
$ mqtt sub -t topic -of publishes.log
```

***

> Subscribe to a topic and output the received publish messages to the file ``publishes.log`` in a specified ``/usr/local/var`` directory

> **NOTE**: If the file is not created yet it will be created by the CLI. If it is present the received publish messages will be appended to the file.
```
$ mqtt sub -t topic -of /usr/local/var/publishes.log
```

***

> Subscribe to a topic and output all the received messages in base64 encoding

```
$ mqtt sub -t topic -b64
```
