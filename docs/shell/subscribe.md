---
layout: default
title: Subscribe
parent: Shell-Mode
nav_order: 4
---

{:.main-header-color-yellow}
# Subscribe
***

The subscribe with a context subscribes the currently active context client to the given topics.
By default it doesn't block the console like the [Subscribe](/docs/04_subscribe) without a context does.
To enable this behavior you can use the **-s** option.


## Synopsis

```
clientID@host> sub  {   -t <topics> [-t <topics>]...
                        [-q <qos>]...
                        [-s]
                        [-b64]
                        [-oc]
                        [-of <receivedMessagesFile>]
                        [-up <subscribeUserProperties>]
}
```

***

##  Options

|Option    |Long Version | Explanation                  | Default  |
|----------|-------------|------------------------------|----------|
| ``-t``   | ``--topic``| The MQTT topic the client will subscribe to. |
| ``-q`` | ``--qos`` | Use a defined quality of service level on all topics if only one QoS is specified. You can define a specific QoS level for every topic. The corresponding QoS levels will be matched in order to the given topics. | ``0``
| ``-s``   | ``--stay``| The subscribe will block the console and wait for publish messages to print. <br><br> Press Enter to interrupt the blocking session and unsubscribe.  | ``false`` |
| ``-oc``| ``--outputToConsole`` | If this flag is set the output will be printed to the console. | ``False`` 
| ``-of``| ``--outputToFile`` | If a file is given print the received publishes to the specified output file. If the file is not present it will be created. |
| ``-b64``| ``--base64``| If set the received publish messages will be base64 encoded. | ``False``
| ``-up``  | ``--subscribeUserProperties`` | User properties of the subscribe message can be defined like ``key=value`` for single pair or ``key1=value1Subscribekey2=value2`` for multiple pairs. |


## Examples

> Subscribe to test topic on default settings (output will be written to Logfile.
See [Logging](/docs/06_logging)):

```
hivemq-cli> con -i myClient
myClient@localhost> sub -t test
```

***

> Subscribe to test topic on default settings, block console and write received publishes to console:

```
myClient@localhost> pub -t test -m Hello -r
myClient@localhost> sub -t test -s
Hello
...
```