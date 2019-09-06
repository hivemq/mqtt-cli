---
layout: default
title: Shell
nav_order: 5
has_children: true
---


{:.main-header-color-yellow}
# Shell Mode
***

Open HiveMQ-CLI in an interactive shell session.
The shell uses **[JLine](https://github.com/jline/jline3)** for handling console input.
Therefore **tab-completion**, **command-history**, **password-masking** and other familiar shell features are available.

The Shell-Mode is based around a client context driven use case.
Therefore methods like Connect and Disconnect switch the current context of the shell and commands like Publish and Subscribe always relate to the currently active client context.

## Example

```
hivemq-cli shell                # starts the shell

hivemq-cli> con -i myClient     # connect client with identifier
myClient> pub -t test -m msg    # publish with new context client
myClient> dis                   # disconnect and remove context
hivemq-cli> ...
```

***

## Summary

Start interactive shell with:
```
$ hivemq-cli shell
```

In Shell-Mode the following Commands are available **without** an active context:

* [Connect](shell/connect)
* [Disconnect](shell/disconnect)
* [Switch](shell/switch)
* [List](shell/list)
* [Clear](shell/clear)
* [Exit](shell/exit)

In Shell-Mode the following Commands are available **with** an active context:

* [Publish](shell/publish)
* [Subscribe](shell/subscribe)
* [Unsubscribe](shell/unsubscribe)
* [Disconnect](shell/disconnect)
* [Switch](shell/switch)
* [List](shell/list)
* [Clear](shell/clear)
* [Exit](shell/exit)



> **NOTE**: A client is uniquely identified in the CLI by the **hostname** and the unique **identifier**.
















