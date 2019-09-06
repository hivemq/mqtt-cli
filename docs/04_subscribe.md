---
layout: default
title: Subscribe
nav_order: 4
has_children: true
---

{:.main-header-color-yellow}
# Subscribe
*** 
Subscribes a client to one or more topics.
If the Subscribe command is not called in Shell-Mode it will block the console by default and write the received publishes to the console.

## Simple Examples

 
|Command                                         |Explanation                                                              |
|------------------------------------------------|-------------------------------------------------------------------------|
| ``hivemq-cli sub -t topic`` | Subscribe to a topic on default settings and block the console.
| ``hivemq-cli sub -t test1 -t test2``| Subscribe to the topics test1 and test2 on default settings and block the console.
| ``hivemq-cli sub -t test -h localhost -p 1884``| Subscribe to topic test at localhost:1884.


See also 
```
hivemq-cli sub --help
```

## Synopsis

```
hivemq-cli sub {    -t <topic> [-t <topic>]...
                    [-q <qos>]...
                    [-b64]
                    [-oc]
                    [-of <receivedMessagesFile>]
                    [-d]
                    [-v]
                    [-up <userProperties>]
                    [-h <host>]
                    [-p <port>]
                    [-V <version>]
                    [-i <identifier>]
                    [-ip <identifierPrefix>]
                    [-c]               
                    [-k <keepAlive>]
                    [-Ce <connectSessionExpiryInterval>]
                    [-Cup <connectUserProperties>]
                    [-s]
                    [-u <user>]
                    [-pw [<password>]]
                    [--cert <clientCertificate> --key <clientPrivateKey>]
                    [--cafile FILE]
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
                    [-Wup <willUserProperties>]
                    [--rcvMax <receiveMaximum>]
                    [--sendMax <sendMaximum>]
                    [--maxPacketSize <maximumPacketSize>]
                    [--sendMaxPacketSize <sendMaximumPacketSize>]
                    [--sendTopicAliasMax <sendTopicAliasMaximum>]
                    [--topicAliasMax <topicAliasMaximum>]
                    [--[no-]reqProblemInfo]
                    [--[no-]reqResponseInfo]
}
```
***

### Subscribe Options

 
|Option    |Long Version                    | Explanation                                        | Default
|----------|--------------------------------|--------------------------------------------------------------------------------------|--------------|
| ``-t``   | ``--topic``| The MQTT topic the client will subscribe to. |
| ``-q`` | ``--qos`` | Use a defined quality of service level on all topics if only one QoS is specified. You can define a specific QoS level for every topic. The corresponding QoS levels will be matched in order to the given topics. | ``0``
| ``-of``| ``--outputToFile`` | If a file is given print the received publishes to the specified output file. If the file is not present it will be created. |
| ``-oc``| ``--outputToConsole`` | If this flag is set the output will be printed to the console. | ``False`` 
| ``-b64``| ``--base64``| If set the received publish messages will be base64 encoded. | ``False``
| ``-up``  | ``--userProperties`` | User properties of the subscribe message can be defined like ``key=value`` for single pair or ``key1=value1\|key2=value2`` for multiple pairs. |

***

## Connect Options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-h``   | ``--host``| The MQTT host. | ``localhost``
| ``-p``  | ``--port``| The MQTT port. | ``1883``
| ``-V``   | ``--version``| The MQTT version can be set to 3 or 5. | ``MQTT  v.5.0``
| ``-i``   | ``--identifier`` | A unique client identifier can be defined. | A randomly defined UTF-8 String will be generated.
| ``-ip``  | ``--identifierPrefix``| The prefix identifier which will prepend the randomly generated client name if no identifier is given. | ``hmqClient``
| ``-c``   | ``--[no-]cleanStart`` | Enable clean start if set. | ``True``
| ``-Ce``  | ``--connectSessionExpiry`` | Session expiry value in seconds. | ``0`` (No Expiry)
| ``-Cup``  | ``--connectUserProperties`` | User properties of the connect message can be defined like <br> ``key=value`` for single pair or ``key1=value1\|key2=value2`` for multiple pairs. |

***

## Security Options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-s``    | ``--secure``  | Use the default SSL configuration. | ``False``
| ``-u``   | ``--user`` | A username for authentication can be defined. |
| ``-pw``  | ``--password`` | A password for authentication can be defined directly. <br> If left blank the user will be prompted for the password in console. |
|   |   ``--cert``  |   The path to the client certificate to use for client-side authentication. |
|   |   ``--key``   |   The path to the client certificate corresponding  private key to use for client-side authentication.    |
|   | ``--cafile``    | Path to a file containing a trusted CA certificate to enable encrypted certificate based communication. |
|   | ``--capath``  | Path to a directory containing trusted CA certificates to enable encrypted certificate based communication. |
|   | ``--ciphers``  | The supported cipher suites in IANA string format concatenated by the ':' character if more than one cipher should be supported. <br> e.g ``TLS_CIPHER_1:TLS_CIPHER_2`` <br> See https://www.iana.org/assignments/tls-parameters/tls-parameters.xml for supported cipher suite strings. |
|   |   ``--tls-version``   |   The TLS version to use - ``TLSv1.1`` ``TLSv1.2`` ``TLSv1.3`` | ``TLSv1.2`` |

*** 

## Will Options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-Wd`` | ``--willDelayInterval`` | Will delay interval in seconds. | ``0``
| ``-We``   | ``--willMessageExpiryInterval``   | Lifetime of the will message in seconds. <br> Can be disabled by setting it to ``4_294_967_295``| ``4_294_967_295`` (Disabled)
| ``-Wm``  | ``--willPayload`` | Payload of the will message. |
| ``-Wq``   | ``--willQualityOfService`` | QoS level of the will message. | ``0``
| ``-Wr``   | ``--[no-]willRetain``  | Retain the will message. | ``False``
| ``-Wt``  | ``--willTopic`` | Topic of the will message.  |
| ``-Wcd``  | ``--willCorrelationData`` | Correlation data of the will message  |
| ``-Wct``   | ``--willContentType`` |   Description of the will message's content. |
| ``-Wpf``  | ``--willPayloadFormatIndicator`` |Payload format can be explicitly specified as ``UTF8`` else it may be ``UNSPECIFIED``. |
| ``-Wrt``  | ``--willResponseTopic`` | Topic Name for a response message.   |
| ``-Wup``   | ``--willUserProperties``  | User properties of the will message can be defined like <br> ``key=value`` for single pair or ``key1=value1\|key2=value2`` for multiple pairs. |

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
|   |  ``--[no-]reqResponseInfo``  | The client requests response information from the server. | ``false``

*** 

## Further Examples

Subscribe to one topic with default QoS Exactly Once:

> **NOTE:** If you only specify one QoS but more than one topic the QoS will be used as default QoS for all topics.

```
$ hivemq-cli sub -t topic1 -t topic2 -q 2
```

Subscribe to the given topics with a QoS specified for each: (topic1 will have QoS 0, topic2 QoS 1, topic2 QoS 2)

```
$ hivemq-cli sub -t topic1 -t topic2 -t topic3 -q 0 -q 1 -q 2
```

Subscribe to a topic and output the received publish messages to the file ``publishes.log`` in the current directory:

`NOTE:` If the file is not created yet it will be created by the CLI. If it is present the received publish messages will be appended to the file.

```
$ hivemq-cli sub -t topic -of publishes.log
```

Subscribe to a topic and output the received publish messages to the file ``publishes.log`` in a specified ``/usr/local/var`` directory:

```
$ hivemq-cli sub -t topic -of /usr/local/var/publishes.log
```

Subscribe to a topic and output all the received messages in base64 encoding:

```
$ hivemq-cli sub -t topic -b64
```