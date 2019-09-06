---
layout: default
title: Shell
nav_order: 5
has_children: true
---


{:.main-header-color-yellow}
# Shell Mode
***

Open HiveMQ-CLI in an interactive shell session.
The shell uses **[JLine](https://github.com/jline/jline3)** for handling console input.
Therefore **tab-completion**, **command-history**, **password-masking** and other familiar shell features are available.

The Shell-Mode is based around a client context driven use case.
Therefore methods like Connect and Disconnect switch the current context of the shell and commands like Publish and Subscribe always relate to the currently active client context.

## Example

```
hivemq-cli shell                # starts the shell

hivemq-cli> con -i myClient     # connect client with identifier
myClient> pub -t test -m msg    # publish with new context client
myClient> dis                   # disconnect and remove context
hivemq-cli> ...
```

***

## Summary

Start interactive shell with:
```
$ hivemq-cli shell
```

In Shell-Mode the following Commands are available **without** an active context:

* [Connect](shell/connect)
* [Disconnect](shell/disconnect)
* [Switch](shell/switch)
* [List](shell/list)
* [Clear](shell/clear)
* [Exit](shell/exit)

In Shell-Mode the following Commands are available **with** an active context:

* [Publish](shell/publish)
* [Subscribe](shell/subscribe)
* [Unsubscribe](shell/unsubscribe)
* [Disconnect](shell/disconnect)
* [Switch](shell/switch)
* [List](shell/list)
* [Clear](shell/clear)
* [Exit](shell/exit)



> **NOTE**: A client is uniquely identified in the CLI by the **hostname** and the unique **identifier**.




***

## Command Overview

### Connect

Please refer to [Connect](shell/connect) for usage information.

Connect switches the current client context to the newly connected client.

```
hivemq-cli> con -i clientID

clientID@localhost>
```

> **NOTE**: The **--debug** and **--verbose** options are overridden by the default values of the shell.

#### Examples:

Connect a client which is identified by myClient and disconnect it afterwards using default settings:

```
hivemq-cli> con -i myClient
myClient@localhost> dis
hivemq-cli>
```

Connect a client which is identified by myClient on specific settings and disconnect it afterwards:

NOTE: Besides the **identifier** also **hostname** has to be given to uniquely identify the client.
If you don't specify these the default settings for these attributes will be used which may lead to unexpected behavior.

```
hivemq-cli> con -i myClient -h broker.hivemq.com -V 3
myClient@localhost> exit  # client is still connected
hivemq-cli> dis -i myClient -h broker.hivemq.com -V 3
```

### [Publish](shell/publish)

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

### Subscribe (with context)

The subscribe with a context subscribes the currently active context client to the given topics.
By default it doesn't block the console like the [Subscribe](mqtt_commands/subscribe.md) without a context does.
To enable this behavior you can use the **-s** option.

#### Synopsis

```
clientID> sub   -t <topics> [-t <topics>]...
                [-q <qos>]...
                [-s]
                [-b64]
                [-oc]
                [-of <receivedMessagesFile>]


```

####  Options

See [Subscribe](mqtt_commands/subscribe.md)

|Option    |Long Version | Explanation                  | Default  |
|----------|-------------|------------------------------|----------|
| ``-s``   | ``--stay``| The subscribe will block the console and wait for publish messages to print.  | ``false`` |


#### Example:

Subscribe to test topic on default settings (output will be written to Logfile.
See [Logging]):

```
hivemq-cli> con -i myClient
myClient@localhost> sub -t test
```

Subscribe to test topic on default settings, block console and write received publishes to console:

```
myClient@localhost> pub -t test -m Hello -r
myClient@localhost> sub -t test -s
Hello
...
```

### Unsubscribe 

Unsubscribes the currently active context client from a list of topics.

See [Unsubscribe](shell/unsubscribe.md)

#### Examples:

Connect a client which is identified by myClient and subscribe it to two topics afterwards.
Then unsubscribe from one of the two topics:

```
hivemq-cli> con -i myClient
myClient@localhost> sub -t topic1 -t topic2
myClient@localhost> unsub -t topic1
hivemq-cli>
```

### Switch MQTT Client context 

Switches the currently active context client.

#### Synopsis

```
{ hmq | clientID }> switch  [<contextName>]
                            -i <identifier>
                            [-h <host>]
```

#### Parameters

|Parameter Name | Explanation | Examples |
|---------------|-------------|------------------------------|
| ``contextName``   | The context name of a client consisting of the the client identifier concatenated by a @ with the hostname. The hostname may be omitted and will be filled with the default host. | `myClient@localhost`  `client2@broker.hivemq.com` or simply the `clientID` (default @localhost will be added)


#### Options

|Option |Long Version | Explanation | Default
|---------------|-------------|------------------------------|
| ``-i``   | ``--identifier``| The unique identifier of a client. |
| ``-h``| ``--host`` | The host the client is connected to. | ``localhost``

#### Example

Connect two clients and switch the active context to the first connected client:

```
hivemq-cli> con -i client1
client1@localhost> exit
hivemq-cli> con -i client2 -h broker.hivemq.com
client2@broker.hivemq.com> switch client1
client1@localhost> switch client2@broker.hivemq.com
client2@broker.hivemq.com>
```

### Exit

Exits the currently active client context.

#### Synopsis

```
clientID> exit
```

#### Example

```
hivemq-cli> con -i client
client@localhost> exit
hivemq-cli>
```

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

### Clear

Clears the terminal screen.

#### Synopsis

```
{ hmq | clientID }> clear
```

#### Example

```
hivemq-cli> clear
```
