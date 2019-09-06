---
layout: default
title: List
parent: Shell
nav_order: 6
--- 

### List

Lists all the connected clients.

#### Synopsis

```
{ hmq | clientID }> ls [-t <sort-by-time>]
                        [-a <all>]

```

#### Options

|Option |Long Version | Explanation | Default
|---------------|-------------|------------------------------|
| ``-t``   | ``--time``| Sort the clients by their creation time. | ``False``
| ``-a``    | ``--all`` | Show detailed information about the clients. | ``False``


#### Examples

Connect two clients and list them by default settings:

```
hivemq-cli> con -i client1
hivemq-cli> con -i client2
hivemq-cli> ls
Client-ID            Server-Address
client1              localhost:1883
client2              localhost:1883
```

Connect a client and show detailed information about it:

```
hivemq-cli> con -i client
hivemq-cli> ls -a
Created-At                     Client-ID            Host                 Port       Server-Address            MQTT version    SSL
2019-08-21T10:47:35.745179     client               localhost            1883       localhost:1883            MQTT_5_0        false
```

NOTE: The list options can be combined in a single command.
So **-at** and **-ta** are valid options.