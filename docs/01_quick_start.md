---
layout: default
title: Quick Start
nav_order: 1
search_exclude: true
---

{:.main-header-color-yellow}
# Quick Start
***
## Usage

To install HiveMQ-CLI on your system please follow the [Installation instructions](02_installation).

The easiest way to start the CLI is by typing:
``` $ hivemq-cli ```
See also ``$ hivemq-cli --help``.

With this you get an output on how to use HiveMQ CLI:
```
Usage:  hivemq-cli [-hV] { pub | sub | shell }

HiveMQ MQTT Command Line Interpreter.

Options:
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:
  shell, sh       Starts HiveMQ-CLI in shell mode, to enable interactive mode with further sub commands.
  sub, subscribe  Subscribe an mqtt client to a list of topics
  pub, publish    Publish a message to a list of topics

```

## Synopsis 
```
$ hivemq-cli [flags] [METHOD] [OPTION [OPTION]]
```

## Supported commands at start

* [Publish](mqtt_commands/publish.md)
* [Subscribe](mqtt_commands/subscribe.md)
* [Shell](05_shell.md) 

> **NOTE**: As latency-issues may slow down the CLI under **Mac OS X** please verify that you have the entry ``127.0.0.1 localhost your-pc-name`` specified under ``/etc/hosts``.
You can use ``sudo sh -c "echo 127.0.0.1 localhost $(hostname) >> /etc/hosts"`` to append this configuration to your hosts file.

***

## Basic Publish

```
$ hivemq-cli pub -t topic -m "Hello World"
```
This command:
* connects an mqtt client to a broker located on default host (localhost) and default port (1883), 
* publishes a message to a defined topic, 
* disconnects the mqtt client from the broker

> See [Publish](03_publish.md) for a detailed overview of the publish command

***

## Basic Subscribe

> **NOTE**: Subscribe will block the console to output published messages

```
$ hivemq-cli sub -t topic
>
```
This command:
* connects an mqtt client to a broker located on default host (localhost) and default port (1883), 
* stays connected to retrieve messages published to the given topic
* exits and disconnects the client on **Ctrl + C** 

> See [Subscribe](04_subscribe.md) for a detailed overview of the subscribe command

***

### Starting the interactive Shell

```
$ hivemq-cli shell
hivemq-cli>
```

The shell mode enables you to execute more complex MQTT behaviour - see [Shell](05_shell.md) 

