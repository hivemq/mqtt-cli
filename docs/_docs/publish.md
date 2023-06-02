---
nav_order: 3
redirect_from: /docs/publish.html
---

# Publish

***

Publishes a message to one or more topics.

```
mqtt publish
```

Alias: `mqtt pub`

***

## Simple Examples

| Command                                            | Explanation                                                                      |
|----------------------------------------------------|----------------------------------------------------------------------------------|
| `mqtt pub -t test -m "Hello"`                      | Publish the message `Hello` with topic 'test' using the default settings.        |
| `mqtt pub -t test1 -t test2 -m "Hello Tests"`      | Publish the message `Hello Tests` with topics 'test1' and 'test2'.               |
| `mqtt pub -t test -m "Hello" -h localhost -p 1884` | Publish the message `Hello` with topic 'test' to a broker at localhost:1884.     |
| `mqtt pub -t test -m:file payload.txt`             | Publish the message in payload.txt with topic 'test' using the default settings. |

***

## Options

### Publish options

{% include options/publish-options.md %}

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

Publish a message with QoS level `Exactly Once`

**NOTE**: If you specify one QoS and multiple topics, the QoS will be used for all topics.

```
mqtt pub -t topic1 -t topic2 -q 2
```

***

Publish a message with a specific QoS for each topic. ('topic1' will have QoS 0, 'topic2' QoS 1 and 'topic3' QoS 2)

```
mqtt pub -t topic1 -q 0 -t topic2 -q 1 -t topic3 -q 2
```
