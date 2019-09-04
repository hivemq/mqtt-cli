---
layout: default
title: Shell
parent: Modes
nav_order: 1
---

# Shell Mode

Open HiveMQ-CLI in an interactive shell session.
The shell uses https://github.com/jline/jline3[JLine] for handling console input.
Therefore tab-completion, command-history, password-masking and other familiar shell features are available.

The Shell-Mode is based around a client context driven use case.
Therefore methods like Connect and Disconnect switch the current context of the shell and commands like Publish and Subscribe always relate to the currently active client context.

**Example:**

```
hivemq-cli shell                # starts the shell

hmq> con -i myClient            # connect client with identifier
myClient> pub -t test -m msg    # publish with new context client
myClient> dis                   # disconnect and remove context
hmq> ...
```

### Summary

Start interactive shell with:
```
$ hivemq-cli shell
```

In Shell-Mode the following Commands are available **without** an active context:

* [Connect (in shell)]
* [Disconnect]
* [Switch]
* [List]
* [Clear]

In Shell-Mode the following Commands are available **with** an active context:

* [Publish (with context)]
* [Subscribe (with context)]
* [Unsubscribe]
* [Disconnect]
* [Switch]
* [Exit]
* [List]
* [Clear]



`NOTE:` A client is uniquely identified in the CLI by the **version**, **hostname**, **port** and the unique **identifier**.

### Example Connect (in shell)

Please refer to [Connect](../mqtt_commands/connect.md) for usage information.

Connect switches the current client context to the newly connected client.

```
hmq> con -i clientID

clientID@localhost>
```

`NOTE:` The **--debug** and **--verbose** options are overridden by the default values of the shell.

#### Examples:

Connect a client which is identified by myClient and disconnect it afterwards using default settings:

```
hmq> con -i myClient
myClient@localhost> dis
hmq>
```

Connect a client which is identified by myClient on specific settings and disconnect it afterwards:

NOTE: Besides the **identifier** also **version**, **hostname** and **port** have to be given to uniquely identify the client.
If you don't specify these the default settings for these attributes will be used which may lead to unexpected behavior.

```
hmq> con -i myClient -h broker.hivemq.com -V 3
myClient@localhost> exit  # client is still connected
hmq> dis -i myClient -h broker.hivemq.com -V 3
```

### Publish (with context)

The publish with a context works almost the same as [Publish] but it will not create a new connection and publish with a new client.
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
hmq> con -i myClient
myClient@localhost> pub -t test -m msg
```

### Subscribe (with context)

The subscribe with a context subscribes the currently active context client to the given topics.
By default it doesn't block the console like the [Subscribe](../mqtt_commands/subscribe.md) without a context does.
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

See [Subscribe](../mqtt_commands/subscribe.md)

|Option    |Long Version | Explanation                  | Default  |
|----------|-------------|------------------------------|----------|
| ``-s``   | ``--stay``| The subscribe will block the console and wait for publish messages to print.  | ``false`` |


#### Example:

Subscribe to test topic on default settings (output will be written to Logfile.
See [Logging]):

```
hmq> con -i myClient
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

See [Unsubscribe](../mqtt_commands/unsubscribe.md)

#### Examples:

Connect a client which is identified by myClient and subscribe it to two topics afterwards.
Then unsubscribe from one of the two topics:

```
hmq> con -i myClient
myClient@localhost> sub -t topic1 -t topic2
myClient@localhost> unsub -t topic1
hmq>
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
hmq> con -i client1
client1@localhost> exit
hmq> con -i client2 -h broker.hivemq.com
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
hmq> con -i client
client@localhost> exit
hmq>
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
hmq> con -i client1
hmq> con -i client2
hmq> ls
Client-ID            Server-Address
client1              localhost:1883
client2              localhost:1883
```

Connect a client and show detailed information about it:

```
hmq> con -i client
hmq> ls -a
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
hmq> clear
```
