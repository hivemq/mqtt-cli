---
layout: default
title: Connect
parent: MQTT Commands
nav_order: 1
---

# Connect command

**Connect** builds the basic wrapper of the direct publish and subscribe commands.
Therefore all Connect Options can also be used in publish and subscribe.

`NOTE`: This Command is only accessible in Shell-Mode

The Connect method creates a client and connects it to the specified broker.
The client will stay connected until it is disconnected by the broker or the <<Disconnect>> method is called.
To list all of the connected clients use the <<List>> method.

## Simple Examples

|Command                             |Explanation |
| -----------------------------------|:-----------|
| ``hmq> con ``                      | Creates and connect a new MQTT client with the default settings
| ``hmq> con -v 3 -h myHost``        | Creates and connects an MQTT 3.1.1 client at myHost with the default port
| ``hmq> con -i hmq-client -p 1884`` | Creates and connects an MQTT client at localhost with port 1884 which is identified by "hmq-client".

See also ``hivemq-cli con --help``

### Synopsis

```
hmq> con {  [-h <hostname>]
            [-V <mqtt-version>]
            [-p <port-number>]
            [-i <client-identifier>]
            [-d <debug>]
            [-v <verbose>]
            [-u <username>]
            [-P <password>]
            [-c <clean-session>]
            [-s <use-default-ssl>]
            [-e <session-expiry>]
            [-up <user-properties>]
            [-wt <will-message-topic>]
            [-wq <will-quality-of-service>]
            [-wm <will-message-payload>]
            [-wr <will-retain>]
            [-we <will-expiry>]
            [-wd <will-delay-interval>]
            [-wp <will-payload-format>]
            [-wc <will-content>]
            [-wrt <will-response-topic>]
            [-wcd <will-correlation-data>]
            [-wu <will-user-properties>]
            [--cafile <path-to-certificate>]
            [--capath <path-to-certificate-directory>]
            [--ciphers <tls-ciphersuites>]
            [--tls-version <tls-version>]
            [--cert <path-to-client-certificate>
             --key <path-to-private-key>]
}
```

### Options

|Option   | Long Version   | Explanation               | Default |
| ------- | -------------- | ------------------------- | -------- |
| ``-h``  | ``--host``     | The MQTT host. | ``localhost`` |
| ``-V``  | ``--version``  | The MQTT version can be set to 3 or 5. | ``MQTT  v.5.0``|
| ``-p``  | ``--port``     | The MQTT port. | ``1883`` |
| ``-i``  | ``--identifier`` | A unique client identifier can be defined. | A randomly defined UTF-8 String will be generated.|
| ``-d``  |   ``--debug``    | Print info level debug messages to the console. | ``False``|
| ``-v``  |   ``--verbose``  | Print detailed debug level messages to the console. | ``False``
| ``-u``  | ``--user`` | A username for authentication can be defined. |
| ``-P``  | ``--password`` | A password for authentication can be defined directly. If left blank the user will be prompted for the password in console. | |
| ``-c``  | ``--clean`` | Disable clean start if set. | ``True``| 
| ``-s``  | ``--secure``  | Use the default SSL configuration. | ``False``|
| ``-e``  | ``--sessionExpiry`` | Session expiry value in seconds. | ``0`` (No Expiry)| 
| ``-up`` | ``--userProperties`` | User properties of the connect message can be defined like ``key=value`` for single pair or ``key1=value1\|key2=value2`` for multiple pairs. | |
| ``-wt``  | ``--willTopic`` | Topic of the will message.  | |
| ``-wq``   | ``--willQualityOfService`` | QoS level of the will message. | ``0`` |
| ``-wm``  | ``--willPayload`` | Payload of the will message. | | 
| ``-wr``   | ``--willRetain``  | Retain the will message. | ``False`` |
| ``-we``   | ``--willMessageExpiryInterval``   | Lifetime of the will message in seconds. Can be disabled by setting it to ``4_294_967_295``| ``4_294_967_295`` (Disabled) | |
| ``-wd`` | ``--willDelayInterval`` | Will delay interval in seconds. | ``0`` |
| ``-wp``  | ``--willPayloadFormatIndicator`` |Payload format can be explicitly specified as ``UTF8`` else it may be ``UNSPECIFIED``. | |
| ``-wc`` | ``--willContentType`` |   Description of the will message's content. | |
| ``-wrt``| ``--willResponseTopic`` | Topic Name for a response message.   | |
| ``-wcd``| ``--willCorrelationData`` | Correlation data of the will message  | | 
| ``-wu`` | ``--willUserProperties``  | User properties of the will message can be defined like ``key=value`` for single pair or ``key1=value1\|key2=value2`` for multiple pairs. | | 
|         | ``--cafile``    | Path to a file containing a trusted CA certificate to enable encrypted certificate based communication. | |
|         | ``--capath``  | Path to a directory containing trusted CA certificates to enable encrypted certificate based communication. | |
|         | ``--ciphers``  | The supported cipher suites in IANA string format concatenated by the ':' character if more than one cipher should be supported. e.g ``TLS_CIPHER_1:TLS_CIPHER_2`` See https://www.iana.org/assignments/tls-parameters/tls-parameters.xml for supported cipher suite strings. |
|         |   ``--tls-version``   |   The TLS version to use - ``TLSv1.1`` ``TLSv1.2`` ``TLSv1.3`` | ``TLSv1.2`` |
|   |   ``--cert``  |   The path to the client certificate to use for client-side authentication. | |
|   |   ``--key``   |   The path to the client certificate corresponding  private key to use for client-side authentication.   | |


### Further Examples

Connect a client to myHost on port 1884:

```
hmq> con -h myHost -p 1884
```

Connect a client to the default host on default port using authentication:

```
hmq> con -u username -P password
# Or omit the password to get it prompted
hmq> con -u username -P
Enter value for --password (The password for the client UTF-8 String.):
```

Connect a client with default settings and use it to publish:

```
hmq> con -i myClient
myClient@localhost> pub -t test -m "Hello World"
```

Connect a client with a will message:

```
hmq> con -wt willtopic -wq 2 -wm "Client disconnected ungracefully"
```

Connect a client with SSL using client side and server side authentication with a password encrypted private key:

```
hmq> con --cafile pathToServerCertificate.pem --tls-version TLSv.1.3
         --cert pathToClientCertificate.pem --key pathToClientKey.pem
Enter private key password:
```
