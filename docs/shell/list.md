---
layout: default
title: List
parent: Shell-Mode
nav_order: 6
--- 

{:.main-header-color-yellow}
# List
***

Lists all the connected clients.

## Synopsis

```
hivemq-cli> ls  {   [-at]
}
```

***

## Options

|Option |Long Version | Explanation | Default
|---------------|-------------|------------------------------|
| ``-a``    | ``--all`` | Show detailed information about the clients. | ``False``
| ``-t``   | ``--time``| Sort the clients by their creation time. | ``False``

***

## Examples

> Connect two clients and list them by default settings

```
hivemq-cli> con -i client1
client1@localhost> exit
hivemq-cli> con -i client2
client2@localhost> ls
Client-ID            Server-Address
client1              localhost:1883
client2              localhost:1883
```

> Connect a client and show detailed information about it

```
hivemq-cli> con -i client
client@localhost> ls -a
Created-At                     Client-ID            Host                 Port       Server-Address            MQTT version    SSL
2019-08-21T10:47:35.745179     client               localhost            1883       localhost:1883            MQTT_5_0        false
```
