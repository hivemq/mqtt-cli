---
layout: default
title: Connect
parent: Shell
nav_order: 1
---

{:.main-header-color-yellow}
# Connect
***

The Connect command creates a client and connects it to the specified broker.
The client will stay connected until it is disconnected by the broker or the <<Disconnect>> method is called.
To list all of the connected clients use the <<List>> method.

## Simple Examples

|Command                             |Explanation |
| -----------------------------------|:-----------|
| ``hivemq-cli> con ``                      | Creates and connect a new MQTT client with the default settings
| ``hivemq-cli> con -v 3 -h myHost``        | Creates and connects an MQTT 3.1.1 client at myHost with the default port
| ``hivemq-cli> con -i hmq-client -p 1884`` | Creates and connects an MQTT client at localhost with port 1884 which is identified by "hmq-client".

See also 
```
hivemq-cli con --help
``` 

*** 
## Synopsis
```
hivemq-cli> con {  [-h <host>]
            [-p <port>]
            [-V <version>]
            [-i <identifier>]
            [-ip <identifierPrefix>]
            [-cdsv]
            [-u <user>]
            [-pw [<password>]]
            [-e <connectSessionExpiryInterval>]
            [-k <keepAlive>]
            [-up <connectUserProperties>]
            [--cert <clientCertificate> --key <clientPrivateKey>]
            [--cafile FILE]
            [--capath DIR]...
            [--ciphers <cipherSuites>[:<cipherSuites>...]]...
            [--tls-version <supportedTLSVersions>]...
            [-Wcd <willCorrelationData>]
            [-Wct <willContentType>]
            [-Wd <willDelayInterval>]
            [-We <willMessageExpiryInterval>]
            [-Wm <willMessage>]
            [-Wpf <willPayloadFormatIndicator>]
            [-Wq <willQos>]
            [-Wr]
            [-Wrt <willResponseTopic>]
            [-Wt <willTopic>]
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

## Options


|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-h``   | ``--host``| The MQTT host. | ``localhost``
| ``-p``  | ``--port``| The MQTT port. | ``1883``
| ``-V``   | ``--version``| The MQTT version can be set to 3 or 5. | ``MQTT  v.5.0``
| ``-i``   | ``--identifier`` | A unique client identifier can be defined. | A randomly defined UTF-8 String will be generated.
| ``-ip``  | ``--identifierPrefix``| The prefix identifier which will prepend the randomly generated client name if no identifier is given. | ``hmqClient``
| ``-d``    |   ``--debug``     | Print info level debug messages to the console. | ``False``
| ``-v``    |   ``--verbose``   | Print detailed debug level messages to the console. | ``False``
| ``-c``   | ``--[no-]cleanStart`` | Enable clean start if set. | ``True``
| ``-e``  | ``--sessionExpiry`` | Session expiry value in seconds. | ``0`` (No Expiry)
| ``-up``  | ``--userProperties`` | User properties of the connect message can be defined like <br>``key=value`` for single pair or ``key1=value1\|key2=value2`` for multiple pairs.


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

Connect a client to myHost on port 1884:

```
hivemq-cli> con -h myHost -p 1884
```

Connect a client to the default host on default port using authentication:

```
hivemq-cli> con -u username -P password
# Or omit the password to get it prompted
hivemq-cli> con -u username -P
Enter value for --password (The password for the client UTF-8 String.):
```

Connect a client with default settings and use it to publish:

```
hivemq-cli> con -i myClient
myClient@localhost> pub -t test -m "Hello World"
```

Connect a client with a will message:

```
hivemq-cli> con -wt willtopic -wq 2 -wm "Client disconnected ungracefully"
```

Connect a client with SSL using client side and server side authentication with a password encrypted private key:

```
hivemq-cli> con --cafile pathToServerCertificate.pem --tls-version TLSv.1.3
         --cert pathToClientCertificate.pem --key pathToClientKey.pem
Enter private key password:
```
