---
layout: default
title: Shell-Mode
nav_order: 5
has_children: true
---


{:.main-header-color-yellow}
# Shell-Mode
***

Open MQTT CLI in an interactive shell session.
The shell uses **[JLine](https://github.com/jline/jline3)** for handling console input.
Therefore **tab-completion**, **command-history**, **password-masking** and other familiar shell features are available.

The Shell-Mode is based around a client context driven use case.
Therefore methods like Connect and Disconnect switch the current context of the shell and commands like Publish and Subscribe always relate to the currently active client context.

Start the interactive shell with:
```
$ mqtt shell
```

## Example

```
$ mqtt shell                # starts the shell

mqtt> con -i myClient                # connect client with identifier 'myClient'
myClient@host> pub -t test -m msg    # publish the message 'msg' with the new context client
myClient@host> dis                   # disconnect and remove context
mqtt> ...
```

***

## Summary

### Commands **without** an active context

* [Connect](shell/connect)
* [Disconnect](shell/disconnect)
* [Switch](shell/switch)
* [List](shell/list)
* [Clear](shell/clear)
* [Exit](shell/exit)

### Commands **with** an active context

* [Publish](shell/publish)
* [Subscribe](shell/subscribe)
* [Unsubscribe](shell/unsubscribe)
* [Disconnect](shell/disconnect)
* [Switch](shell/switch)
* [List](shell/list)
* [Clear](shell/clear)
* [Exit](shell/exit)



> **NOTE**: A client is uniquely identified in the CLI by the **hostname** and the unique **identifier**.
















