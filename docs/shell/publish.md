---
layout: default
title: Publish
parent: Shell
nav_order: 3
---

# [Publish](shell/publish)

The publish with a context works almost the same as [Publish](publish) but it will not create a new connection and publish with a new client.
Instead it uses the currently active context client.

#### Synopsis:

```
clientID> pub   -t <topic> [-t <topic>]...
                -m <message>
                [-q <qos>]...
                [-r]
                [-pc <contentType>]
                [-pd <correlationData>]
                [-pe <messageExpiryInterval>]
                [-pf <payloadFormatIndicator>]
                [-pr <responseTopic>]
                [-pu <publishUserProperties>]
```

`NOTE:` The default options are the same as in [Publish]

#### Options

See [Publish]

#### Example

Publish with a client identified with "myClient" to the default settings:

```
hivemq-cli> con -i myClient
myClient@localhost> pub -t test -m msg
```