---
layout: default
title: Disconnect
parent: Shell
nav_order: 2
---

# Disconnect
 
`Note:` This Command is only available in the **shell** mode as in direct commands the client will be automatically connected and disconnected.

Disconnects a previously connected client. 

#### Synopsis (without client context)

```
dis     -i <identifier>
        [-h <hostname>]
        [-e <sessionExpiryInterval>]
        [-r <reasonString>]
        [-up <userProperties>]
```

#### Synopsis (with client context):

Disconnects the currently active client context.

```
dis     [-e <sessionExpiryInterval>]
        [-r <reasonString>]
        [-up <userProperties>]
```

### Options:

 
|Option   | Long Version   | Explanation               | Default  |
| ------- | -------------- | ------------------------- | -------- |
| ``-i``   | ``--identifier``| The unique identifier of a client. |
| ``-h``| ``--host`` | The host the client is connected to. | ``localhost``
| ``-e``  | ``--sessionExpiry`` | Session expiry value in seconds. | ``0`` (No Expiry)
| ``-r``  | ``--reason``| Reason string for the disconnect |
| ``-up`` | ``--userProperties``|  User properties of the disconnect message can be defined like ``key=value`` for single pair or ``key1=value1\|key2=value2`` for multiple pairs.|


#### Examples:

Connect a client which is identified by myClient and disconnect it afterwards using default settings:

```
hivemq-cli> con -i myClient
myClient@localhost> dis
hivemq-cli>
```

Connect a client which is identified by myClient on specific settings and disconnect it afterwards:

`NOTE:` Besides the **identifier** also **version**, **hostname** and **port** have to be given to uniquely identify the client.
If you don't specify these the default settings for these attributes will be used which may lead to unexpected behavior.

```
hivemq-cli> con -i myClient -h broker.hivemq.com -V 3
myClient@localhost> exit  # client is still connected
hivemq-cli> dis -i myClient -h broker.hivemq.com -V 3