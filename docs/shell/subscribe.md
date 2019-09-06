---
layout: default
title: Subscribe
parent: Shell
nav_order: 4
---

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