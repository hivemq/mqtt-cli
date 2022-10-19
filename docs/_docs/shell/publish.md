---
nav_order: 3
redirect_from: /docs/shell/publish.html
---

# Publish
***

The publish with a context works almost the same as [Publish](../publish.md) but it will not create a new connection and publish with a new client.
Instead it uses the currently active context client.

## Synopsis

```
client@host> pub    -t <topics> [-t <topics>]... 
                    (-m <message> | -m:file <filename> | -m:empty)
                    [-q <qos>]... 
                    [-r]
                    [-e <messageExpiryInterval>] 
                    [-cd <correlationData>]                  
                    [-ct <contentType>]
                    [-pf <payloadFormatIndicator>]
                    [-rt <responseTopic>]
                    [-up <userProperties>]...
                    [-h]
```

***

## Options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-t``   | ``--topic``| The MQTT topic where the message will be published. |
| ``-m``| ``--message`` | The message which will be published on the topic. |
| ``-m:file`` | ``--message-file``           | The file whose payload will be published on the topic.                                                                                                                                                                                |
| ``-m:empty`` | ``--message-empty``           | Sets the message to an empty payload.                                                                                                                                                                                |
| ``-q`` | ``--qos`` | Use a defined quality of service level on all topics if only one QoS is specified.<br> You can define a specific QoS level for every topic. The corresponding QoS levels will be matched in order to the given topics. | ``0``
| ``-r``| ``--retain`` | Message will be retained. | ``false``
| ``-e`` | ``--messageExpiryInterval`` | The lifetime of the publish message in seconds. |
| ``-cd`` | ``--correlationData`` | The correlation data of the publish message. |
| ``-ct`` | ``--contentType`` | A description of the content of the publish message. |
| ``-pf`` | ``--payloadFormatIndicator`` | The payload format indicator of the publish message. |
| ``-rt`` | ``--responseTopic`` | The topic name for the response message of the publish message. |
| ``-up`` | ``--userProperty``  | A user property of the publish message. |

***

## Example

> Publish with a client identified with ``myClient`` to the default settings:

```
mqtt> con -i myClient
myClient@localhost> pub -t test -m msg
```