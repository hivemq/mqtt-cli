---
layout: default
title: MQTT Commands
nav_order: 5
has_children: true
---

# MQTT Commands

## Usage

### Synopsis

```
$ hivemq-cli [flags] [METHOD] [OPTION [OPTION]]
```

#### Supported methods at start:

* [Publish](mqtt_commands/publish.md)
* [Subscribe](mqtt_commands/subscribe.md)
* [Shell](modes/shell.md) 


### Examples

**Basic Publish:**

```
$ hivemq-cli pub -t topic -m "Hello World"
```
This command:
* connects an mqtt client to a broker located on default host (localhost) and default port (1883), 
* publishes a message to a defined topic, 
* disconnects the mqtt client from the broker

**Basic Subscribe** (will block the console to output published mesages):

```
$ hivemq-cli sub -t topic
>
```
This command:
* connects an mqtt client to a broker located on default host (localhost) and default port (1883), 
* publishes a message to a defined topic, 
* stays connected to retrieve messages published to the given topic


**Starting the interactive Shell:**

```
$ hivemq-cli shell
hmq>
```

In shell mode you get a couple of commands - see [Shell](modes/shell.md) 

