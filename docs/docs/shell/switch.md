---
layout: default
title: Switch
parent: Shell-Mode
nav_order: 7
--- 

{:.main-header-color-yellow}
# Switch
***

Switches the currently active context client.

## Synopsis

```
mqtt> switch    [<contextName>]
                [-i <identifier>] 
                [-h <host>]            
                [--help] 
```

***

## Parameters

|Parameter Name | Explanation | Examples |
|---------------|-------------|------------------------------|
| ``contextName``   | The context name of a client consisting of the the client identifier concatenated by a @ with the hostname. The hostname may be omitted and will be filled with the default host. | `myClient@localhost`  `client2@broker.hivemq.com` or simply the `clientID` (default @localhost will be added)


***

## Options

|Option |Long Version | Explanation | Default
|---------------|-------------|------------------------------|
| ``-i``   | ``--identifier``| The unique identifier of a client. |
| ``-h``| ``--host`` | The host the client is connected to. | ``localhost``

***

## Example

> Connect two clients and switch the active context to the first connected client

```
mqtt> con -i client1
client1@localhost> exit
mqtt> con -i client2 -h broker.hivemq.com
client2@broker.hivemq.com> switch client1
client1@localhost> switch client2@broker.hivemq.com
client2@broker.hivemq.com>
```