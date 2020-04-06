---
layout: default
title: Test
nav_order: 6
has_children: true
---
{:.main-header-color-yellow}
# Test
***
Runs tests against the specified broker to find out its features and limitations.


By default the test command will use MQTT 3 clients to test the broker first and will afterwards check the connect 
restrictions returned by a connect of a MQTT 5 client. You can alter this behavior by specifying different 
[options](#test-options) when using the command.
 
## Example   

``` 
$ mqtt test -h broker.hivemq.com
MQTT 3: OK
	- Maximum topic length: 65535 bytes
	- QoS 0: Received 10/10 publishes in 85,10ms
	- QoS 1: Received 10/10 publishes in 84,66ms
	- QoS 2: Received 10/10 publishes in 49,48ms
	- Retain: OK
	- Wildcard subscriptions: OK
	- Payload size: >= 100000 bytes
	- Maximum client id length: 65535 bytes
	- Unsupported Ascii Chars: ALL SUPPORTED
MQTT 5: OK
	- Connect restrictions: 
		> Retain: OK
		> Wildcard subscriptions: OK
		> Shared subscriptions: OK
		> Subscription identifiers: OK
		> Maximum QoS: 2
		> Receive maximum: 10
		> Maximum packet size: 268435460 bytes
		> Topic alias maximum: 5
		> Session expiry interval: Client-based
		> Server keep alive: Client-based
```

<!---
See also 
```
mqtt test --help
```
-->

*** 

## Synopsis

``` 
mqtt test   [--help]
            [-V=<version>]
            [-f]
            [-t=<timeOut>]
            [-q=<qosTries>]
            [-s]
            [-pw[=<password>]] 
            [-pw:env[=<passwordFromEnv>]]
            [--cert=<clientCertificate>] 
            [-h=<host>] 
            [--key=<clientPrivateKey>]
            [-p=<port>] 
            [-pw:file=<passwordFromFile>] 
            [-t=<timeOut>] 
            [-u=<user>] 
            [--cafile=FILE]... 
            [--capath=DIR]...
            [--ciphers=<cipherSuites>[:<cipherSuites>...]]... 
            [--tls-version=<supportedTLSVersions>]...
```

***

## Test options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-V``   | ``--mqttVersion``| The MQTT version to test the broker on. | Both versions will be tested
| ``-f``| ``--force`` | Also use force tests to find out MQTT 5 features and limitations, even though connect restrictions should tell these already. | ``False``
| ``-t``| ``--timeOut`` | The time to wait for the broker to respond (in seconds). | ``10s``
| ``-q`` | ``--qosTries`` | The amount of messages to send and receive from the broker for each QoS level. | ``10``

***

## Connect Options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-h``   | ``--host``| The MQTT host. | ``localhost``
| ``-p``  | ``--port``| The MQTT port. | ``1883``

***

## Security Options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-s``    | ``--secure``  | Whether a default SSL configuration is used. | ``False``
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

## Further Examples

> Force test MQTT 5 only (Uses MQTT 5 clients only for tests)

```
$ mqtt test -h broker.hivemq.com -f -V 5
MQTT 5: OK
	- Connect restrictions: 
		> Retain: OK
		> Wildcard subscriptions: OK
		> Shared subscriptions: OK
		> Subscription identifiers: OK
		> Maximum QoS: 2
		> Receive maximum: 10
		> Maximum packet size: 268435460 bytes
		> Topic alias maximum: 5
		> Session expiry interval: Client-based
		> Server keep alive: Client-based
	- Maximum topic length: 65535 bytes
	- QoS 0: Received 10/10 publishes in 56,21ms
	- QoS 1: Received 10/10 publishes in 71,38ms
	- QoS 2: Received 10/10 publishes in 127,12ms
	- Retain: OK
	- Wildcard subscriptions: OK
	- Payload size: >= 100000 bytes
	- Maximum client id length: 65535 bytes
	- Unsupported Ascii Chars: ALL SUPPORTED
```

***

> Test receving of 100 publishes in 10s (for each qos level)

```
$ mqtt test -h broker.hivemq.com -q 100 
...
    - QoS 0: Received 100/100 publishes in 123,44ms
    - QoS 1: Received 100/100 publishes in 223,78ms
    - QoS 2: Received 100/100 publishes in 340,81ms
...
```