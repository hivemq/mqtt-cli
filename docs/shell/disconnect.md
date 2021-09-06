---
layout: default
nav_order: 2
redirect_from: /docs/shell/disconnect.html
---

# Disconnect
***

Disconnects a previously connected client. 

## Synopsis (without client context)

```
mqtt> dis   -i <identifier> 
            [-a]
            [-h <host>]  
            [-e <sessionExpiryInterval>] 
            [-r <reasonString>] 
            [-up <userProperties>]...
```

## Synopsis (with client context)

Disconnects the currently active client context.

```
client@host>  dis   [-a]
                    [-e <sessionExpiryInterval>]
                    [-r <reasonString>]
                    [-up <userProperty>]...
```

***

## Options

 
|Option   | Long Version   | Explanation               | Default  |
| ------- | -------------- | ------------------------- | -------- |
| ``-a``   | ``--all``| Disconnect all connected clients. |
| ``-i``   | ``--identifier``| The unique identifier of a client. |
| ``-h``| ``--host`` | The host the client is connected to. | ``localhost``
| ``-e``  | ``--sessionExpiry`` | Session expiry value in seconds. | ``0`` (No Expiry)
| ``-r``  | ``--reason``| Reason string for the disconnect |
| ``-up`` | ``--userProperty``|  A user property of the disconnect message. |

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
