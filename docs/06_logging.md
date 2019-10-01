---
layout: default
title: Logging
nav_order: 6
---

{:.main-header-color-yellow}
# Logging
***

* By starting MQTT CLI in Shell-Mode a log file will be created and all trace level information will be logged to the file.
* Verbose mode allows you to get more information about each MQTT message and the parameters.
* By default the Shell-Mode logs all commands in verbose mode to a uniquely named logfile which is placed in ``~.mqtt-cli/logs`` which is printed out at the start of the shell.

```
$ mqtt shell 

....
Press Ctl-C to exit.

Writing Logfile to /~/.hivemq.cli/logs/hmq-mqtt-log.<Day>.txt
mqtt>
```



## Usage Example
> If you require debug logging for direct access add the `-d` and `-v` options are available for the basic publish and subscribe command


```
$ mqtt pub -i c1 -t test -m "Hello World" -d 

c1 : sending CONNECT
c1 : received CONNACK SUCCESS
c1 : sending PUBLISH: (Topic: test, QoS AT_MOST_ONCE, Message: 'Hello World')
c1 : received RESULT: 'Hello World' for PUBLISH to Topic:  test

```
