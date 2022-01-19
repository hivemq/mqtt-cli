---
nav_order: 5
redirect_from: /docs/shell.html
---

# Shell-Mode
***

Open MQTT CLI in an interactive shell session.
The shell uses **[JLine](https://github.com/jline/jline3)** for handling console input.
Therefore, **tab-completion**, **command-history**, **password-masking** and other familiar shell features are available.

The Shell-Mode is based around a client context driven use case.
Therefore, methods like Connect and Disconnect switch the current context of the shell and commands like Publish and Subscribe always relate to the currently active client context.

Start the interactive shell with:
```
$ mqtt shell
```

To start the interactive shell with default logging to ``~/.mqtt-cli/logs`` use:
```
$ mqtt shell -l
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

* [Connect](connect)
* [Disconnect](disconnect)
* [Switch](switch)
* [List](list)
* [Clear](clear)
* [Exit](exit)

### Commands **with** an active context

* [Publish](publish)
* [Subscribe](subscribe)
* [Unsubscribe](unsubscribe)
* [Disconnect](disconnect)
* [Switch](switch)
* [List](list)
* [Clear](clear)
* [Exit](exit)



> **NOTE**: A client is uniquely identified in the CLI by the **hostname** and the unique **identifier**.
















