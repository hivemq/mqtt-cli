---
layout: default
title: Publish
parent: MQTT Commands
nav_order: 3
---

# Publish

`NOTE:` Publish supports all Connect options.
Therefore all Connect options can be used with publish.

`NOTE:` In Shell-Mode use the <<Publish (with context)>> equivalent.

Publishes a message to one or more topics.

### Simple Examples


|Command                                                |Explanation                                                              |
|-------------------------------------------------------|-------------------------------------------------------------------------|
| ``hivemq-cli pub -t test -m "Hello" `` | Publish the message "Hello" to the test topics with the default settings
| ``hivemq-cli pub -t test1 -t test2 -m "Hello Tests"`` | Publish the message "Hello Tests" on both test topics with the default settings
| ``hivemq-cli pub -t test -m "Hello" -h localhost -p 1884``| Publish the message "Hello" on localhost:1884|

See also ``hivemq-cli pub --help``

### Synopsis

```
hivemq-cli pub { [[Connect-Option] [Connect-Option]]
                -t <topic> [-t <topic>]...
                -m <message>
                [-q <qos>]...
                [-r]
                [-pc <contentType>]
                [-pd <correlationData>]
                [-pe <messageExpiryInterval>]
                [-pf <payloadFormatIndicator>]
                [-pr <responseTopic>]
                [-pu <publishUserProperties>]
}
```

### Options


|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-t``   | ``--topic``| The MQTT topic where the message will be published. |
| ``-m``| ``--message`` | The message which will be published on the topic. |
| ``-r``| ``--retain`` | Message will be retained. | ``False``
| ``-q`` | ``--qos`` | Use a defined quality of service level on all topics if only one QoS is specified.  You can define a specific QoS level for every topic. The corresponding QoS levels will be matched in order to the given topics. | ``0``
| ``-pc`` | ``--contentType`` | A description of the content of the publish message. |
| ``-pd`` | ``--correlationData`` | The correlation data of the publish message. |
| ``-pe`` | ``--messageExpiryInterval`` | The lifetime of the publish message in seconds. |
| ``-pf`` | ``--payloadFormatIndicator`` | The payload format indicator of the publish message. |
| ``-pr`` | ``--responseTopic`` | The topic name for the response message of the publish message. |
| ``-pu`` | ``--publishUserProperties``  | The user property of the publish message. Usage: Key=Value, Key1=Value1:Key2=Value2 |


### Further Examples

Publish a message with default QoS set to Exactly Once:

`NOTE:` If you only specify one QoS but more than one topic the QoS will be used as default QoS for all topics.

```
$ hivemq-cli pub -t topic1 -t topic2 -q 2
```

Publish a message with a given QoS for each topic. (topic1 will have QoS 0, topic2 QoS 1, topic2 QoS 2):

```
$ hivemq-cli pub -t topic1 -t topic2 -t topic3 -q 0 -q 1 -q 2
```