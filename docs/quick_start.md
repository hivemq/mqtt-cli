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

To install MQTT CLI on your system please follow the [Installation instructions](installation).

The easiest way to start the CLI is by typing:
``` $ mqtt ```
See also ``$ mqtt --help``.

With this you get an output on how to use MQTT CLI:
```
Usage:  mqtt [-hV] { pub | sub | shell | test | hivemq }

MQTT Command Line Interpreter.

Options:
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:
    pub, publish    Publish a message to a list of topics
    sub, subscribe  Subscribe an mqtt client to a list of topics
    shell, sh       Starts MqttCLI in shell mode, to enable interactive mode with further sub commands.
    test            Tests the specified broker on different MQTT feature support and prints the results
    hivemq          HiveMQ Command Line Interpreter.

```

## Synopsis 
```
$ mqtt [flags] [METHOD] [OPTION [OPTION]]
```

## Supported commands at start

* [Publish](publish)
* [Subscribe](subscribe)
* [Shell](shell)
* [Test](test) 
* [HiveMQ](hivemq)
* [Swarm](swarm) 

***

## Basic Publish

```
$ mqtt pub -t topic -m "Hello World"
```
This command:
* connects an mqtt client to a broker located on default host (localhost) and default port (1883), 
* publishes a message to a defined topic, 
* disconnects the mqtt client from the broker

> See [Publish](publish) for a detailed overview of the publish command

***

## Basic Subscribe

```
$ mqtt sub -t topic
>
```
This command:
* connects an mqtt client to a broker located on default host (localhost) and default port (1883), 
* stays connected to retrieve messages published to the given topic
* exits and disconnects the client on **Ctrl + C** 

> See [Subscribe](subscribe) for a detailed overview of the subscribe command

***

## Starting the interactive Shell

```
$ mqtt shell
...
mqtt>
```

The shell mode enables you to execute more complex MQTT behaviour - see [Shell](shell) 

## Testing a MQTT broker

``` 
$ mqtt test
...
```

This command runs a quick test suite against the broker running on the default host on the default port.
The results are printed to the console.

## HiveMQ command line
```
$ mqtt hivemq

Usage:  mqtt hivemq [-hV] [COMMAND]

HiveMQ Command Line Interpreter.

Options:
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:
  export  Exports the specified details from HiveMQ
```

This command gives access to the HiveMQ command line which offers commands to interact with a  running HiveMQ instance.

# HiveMQ Swarm
***

The HiveMQ Swarm command offers various ways to interact with HiveMQ Swarm.

```
$ mqtt swarm
Usage:  mqtt swarm [-hV] [COMMAND]

HiveMQ Swarm Command Line Interpreter.

Options:
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:
  status  Check the status of HiveMQ Swarm.
  run     HiveMQ Swarm Run Command Line Interpreter.
```