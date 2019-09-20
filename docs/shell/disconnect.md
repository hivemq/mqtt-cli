---
layout: default
title: Disconnect
parent: Shell-Mode
nav_order: 2
---

{:.main-header-color-yellow}
# Disconnect
***

Disconnects a previously connected client. 

## Synopsis (without client context)

```
mqtt> dis {   -i <identifier>
                    [-h <hostname>]
                    [-e <sessionExpiryInterval>]
                    [-r <reasonString>]
                    [-up <userProperties>]
}
```

## Synopsis (with client context)

Disconnects the currently active client context.

```
clientID@host>  dis {   [-e <sessionExpiryInterval>]
                        [-r <reasonString>]
                        [-up <userProperties>]
}
```

***

## Options

 
|Option   | Long Version   | Explanation               | Default  |
| ------- | -------------- | ------------------------- | -------- |
| ``-i``   | ``--identifier``| The unique identifier of a client. |
| ``-h``| ``--host`` | The host the client is connected to. | ``localhost``
| ``-e``  | ``--sessionExpiry`` | Session expiry value in seconds. | ``0`` (No Expiry)
| ``-r``  | ``--reason``| Reason string for the disconnect |
| ``-up`` | ``--userProperties``|  User properties of the disconnect message can be defined like ``key=value`` for single pair or ``key1=value1|key2=value2`` for multiple pairs.|

***

## Examples

> Connect a client which is identified by myClient and disconnect it afterwards using default settings

```
mqtt> con -i myClient
myClient@localhost> dis
mqtt>
```

***

> Connect a client which is identified by myClient on specific settings and disconnect it afterwards

```
mqtt> con -i myClient -h broker.hivemq.com
myClient@localhost> exit  # client is still connected
mqtt> dis -i myClient -h broker.hivemq.com
```

> **NOTE**: Besides the **identifier** also the **hostname** has to be given to uniquely identify the client.
If you don't specify these the default settings for these attributes will be used which may lead to unexpected behavior.
