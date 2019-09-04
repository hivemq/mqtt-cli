---
layout: default
title: Quick Start
nav_order: 1
search_exclude: true
---

# Quick Start

## Usage

The simplest way to start the CLI is typing:
``` $ hivemq-cli ```
See also ``hivemq-cli --help``.

This results in the output of how to use:
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

### Synopsis
```
$ hivemq-cli [flags] [METHOD] [OPTION [OPTION]]
```

#### Supported methods at start:

* [Publish](mqtt_commands/publish.md)
* [Subscribe](mqtt_commands/subscribe.md)
* [Shell](modes/shell.md) 




`Note:` As latency-issues may slow down the CLI under **Mac OS X** please verify that you have the entry ``127.0.0.1 localhost your-pc-name`` specified under ``/etc/hosts``.
You can use ``sudo sh -c "echo 127.0.0.1 localhost $(hostname) >> /etc/hosts"`` to append this configuration to your hosts file.
