---
nav_order: 6
redirect_from: /docs/test.html
---

# Broker Test

***

Runs tests against the specified broker to find out its features and limitations.

```
mqtt test
```

By default, the test command will use MQTT 3 clients to test the broker first and will afterward check the connect
restrictions returned by a connect of a MQTT 5 client. You can alter this behavior by specifying different
[options](#options) when using the command.

## Example

``` 
$ mqtt test -h broker.hivemq.com
MQTT 3: OK
    - Maximum topic length: 65535 bytes
    - QoS 0: Received 10/10 publishes in 47,59ms
    - QoS 1: Received 10/10 publishes in 68,87ms
    - QoS 2: Received 10/10 publishes in 43,18ms
    - Retain: OK
    - Wildcard subscriptions: OK
    - Shared subscriptions: OK
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

***

## Options

| Option | Long Version    | Explanation                                                                    | Default                      |
|--------|-----------------|--------------------------------------------------------------------------------|------------------------------|
| `-V`   | `--mqttVersion` | The MQTT version to test the broker on.                                        | Both versions will be tested |
| `-a`   | `--all`         | Perform all tests for all MQTT versions.                                       | `false` (Only test MQTT 3)   |
| `-t`   | `--timeOut`     | The time to wait for the broker to respond (in seconds).                       | `10`                         |
| `-q`   | `--qosTries`    | The amount of messages to send and receive from the broker for each QoS level. | `10`                         |

### Connect Options

| Option | Long Version | Explanation    | Default     |
|--------|--------------|----------------|-------------|
| `-h`   | `--host`     | The MQTT host. | `localhost` |
| `-p`   | `--port`     | The MQTT port. | `1883`      |

### Security Options

#### Credentials Authentication

{% include options/authentication-options.md %}

#### TLS Authentication

{% include options/tls-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=false %}

*** 

## Further Examples

Perform tests for MQTT 5 only

```
$ mqtt test -h broker.hivemq.com -a -V 5
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
    - QoS 0: Received 10/10 publishes in 52,70ms
    - QoS 1: Received 10/10 publishes in 79,95ms
    - QoS 2: Received 10/10 publishes in 125,65ms
    - Retain: OK
    - Wildcard subscriptions: OK
    - Shared subscriptions: OK
    - Payload size: >= 100000 bytes
    - Maximum client id length: 65535 bytes
    - Unsupported Ascii Chars: ALL SUPPORTED
```

***

Test receiving of 100 publishes in 10s (for each qos level)

```
$ mqtt test -h broker.hivemq.com -q 100 
...
    - QoS 0: Received 100/100 publishes in 123,44ms
    - QoS 1: Received 100/100 publishes in 223,78ms
    - QoS 2: Received 100/100 publishes in 340,81ms
...
```
