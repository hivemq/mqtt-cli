---
nav_order: 4
redirect_from: /docs/subscribe.html
---

# Subscribe

*** 

Subscribes a client to one or more topics.
If the Subscribe command is not called in Shell-Mode, it will block the console and write the received publishes to the
console.

```
$ mqtt subscribe
```

Alias: `$ mqtt sub`

***

## Simple Examples

| Command                                 | Explanation                                                                              |
|-----------------------------------------|------------------------------------------------------------------------------------------|
| `mqtt sub -t topic`                     | Subscribe to a topic with default settings and block the console.                        |
| `mqtt sub -t test1 -t test2`            | Subscribe to the topics 'test1' and 'test2' with default settings and block the console. |
| `mqtt sub -t test -h localhost -p 1884` | Subscribe to topic 'test' at a broker with the address 'localhost:1884'.                 |

***

## Options

### Subscribe Options

{% include options/subscribe-options.md %}

### Connect Options

{% include options/connect-options.md %}

#### Will Options

{% include options/will-options.md %}

#### Connect Restrictions

{% include options/connect-restrictions-options.md %}

### Security Options

#### Credentials Authentication

{% include options/authentication-options.md %}

#### TLS Authentication

{% include options/tls-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Debug Options

{% include options/debug-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=false %}

*** 

## Further Examples

Subscribe to one topic with QoS level `Exactly Once`.

**NOTE**: If you specify one QoS and multiple topics, the QoS will be used for all topics.

```
mqtt sub -t topic1 -t topic2 -q 2  
```

***

Subscribe to the given topics with a specific QoS for each topic.
(`topic1` will have QoS 0, `topic2` QoS 1 and `topic3` QoS 2)

```
mqtt sub -t topic1 -q 0 -t topic2 -q 1 -t topic3 -q 2
```

***

Subscribe to a topic and output the received publish messages to the file `publishes.log` in the current directory.

**NOTE**: The MQTT CLI creates the file if it does not exist. Received publish messages will be appended.

```
mqtt sub -t topic -of publishes.log
```

***

Subscribe to a topic and output the received publish messages to the file `publishes.log` in a
specified `/usr/local/var` directory.

**NOTE**: The MQTT CLI creates the file if it does not exist. Received publish messages will be appended.

```
mqtt sub -t topic -of /usr/local/var/publishes.log
```

***

Subscribe to a topic and output all the received messages in base64 encoding.

```
mqtt sub -t topic -b64
```
