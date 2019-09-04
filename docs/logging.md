---
layout: default
title: Logging
nav_order: 5
---

# Logging


* Starting the hivemq-cli in shell mode a log file will be always created and all Information will be logged to the file.
* Verbose mode allows you to get more information about each MQTT Message and the parameters.
* By default the Shell-Mode logs all commands in verbose mode to a uniquely named logfile which is placed in a temp directory which is printed out at the start of the shell.

```
$ ./hivemq-cli.sh shell 

....
Press Ctl-C to exit.

Writing Logfile to /var/folders/<temp-folder>/hmq-mqtt-log.<Day>.txt
hivemq-cli>
```

* If debug logging for direct access is needed add the -d to your command.

## Usage Example

```
$ ./hivemq-cli.sh pub -i c1 -t test -m "Hello World" -d 

c1 : sending CONNECT
c1 : received CONNACK SUCCESS
c1 : sending PUBLISH: (Topic: test, QoS AT_MOST_ONCE, Message: 'Hello World')
c1 : received RESULT: 'Hello World' for PUBLISH to Topic:  test

```
