---
layout: default
title: Subscribe
parent: MQTT Commands
nav_order: 2
---

# Subscribe

`NOTE:` Subscribe supports all Connect options.
Therefore all Connect options can be used with subscribe.

`NOTE:` In Shell-Mode use the <<Subscribe (with context)>> equivalent.

Subscribes a client to one or more topics.
If the Subscribe command is not called in Shell-Mode it will block the console by default and write the received publishes to the console.

### Simple Examples

 
|Command                                         |Explanation                                                              |
|------------------------------------------------|-------------------------------------------------------------------------|
| ``hivemq-cli sub -t topic`` | Subscribe to a topic on default settings and block the console.
| ``hivemq-cli sub -t test1 -t test2``| Subscribe to the topics test1 and test2 on default settings and block the console.
| ``hivemq-cli sub -t test -h localhost -p 1884``| Subscribe to topic test at localhost:1884.


See also ``hivemq-cli sub --help``

### Synopsis

```
hivemq-cli sub { [[Connect-Option] [Connect-Option]]
                -t <topic> [-t <topic>]...
                [-q <qos>]...
                [-sup <userProperties>]
                [-b64]
                [-oc]
                [-of <receivedMessagesFile>]
}
```

### Options

 
|Option    |Long Version                    | Explanation                                        | Default
|----------|--------------------------------|--------------------------------------------------------------------------------------|--------------|
| ``-t``   | ``--topic``| The MQTT topic the client will subscribe to. |
| ``-q`` | ``--qos`` | Use a defined quality of service level on all topics if only one QoS is specified. You can define a specific QoS level for every topic. The corresponding QoS levels will be matched in order to the given topics. | ``0``
| ``-sup``  | ``--subscribeUserProperties`` | User properties of the subscribe message can be defined like ``key=value`` for single pair or ``key1=value1\|key2=value2`` for multiple pairs. |
| ``-of``| ``--outputToFile`` | If a file is given print the received publishes to the specified output file. If the file is not present it will be created. |
| ``-oc``| ``--outputToConsole`` | If this flag is set the output will be printed to the console. | ``False`` in Shell-Mode, else ``True``
| ``-b64``| ``--base64``| If set the received publish messages will be base64 encoded. | ``False``

### Further Examples

**Subscribe** to one topic with default QoS Exactly Once:

`NOTE:` If you only specify one QoS but more than one topic the QoS will be used as default QoS for all topics.

```
$ hivemq-cli sub -t topic1 -t topic2 -q 2
```

**Subscribe** to the given topics with a QoS specified for each: (topic1 will have QoS 0, topic2 QoS 1, topic2 QoS 2)

```
$ hivemq-cli sub -t topic1 -t topic2 -t topic3 -q 0 -q 1 -q 2
```

**Subscribe** to a topic and output the received publish messages to the file ``publishes.log`` in the current directory:

`NOTE:` If the file is not created yet it will be created by the CLI. If it is present the received publish messages will be appended to the file.

```
$ hivemq-cli sub -t topic -of publishes.log
```

**Subscribe** to a topic and output the received publish messages to the file ``publishes.log`` in a specified ``/usr/local/var`` directory:

```
$ hivemq-cli sub -t topic -of /usr/local/var/publishes.log
```

**Subscribe** to a topic in Shell-Mode and output all the received publish messages to the console:

```
hmq> sub -t topic -oc
```

**Subscribe** to a topic and output all the received messages in base64 encoding:

```
$ hivemq-cli sub -t topic -b64
