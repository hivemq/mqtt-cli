---
nav_order: 5
redirect_from: /docs/shell.html
---

# Shell-Mode

***

Open MQTT CLI in an interactive shell session.
The shell uses **[JLine](https://github.com/jline/jline3)** for handling console input.
Therefore, **tab-completion**, **command-history**, **password-masking** and other familiar shell features are
available.

The Shell-Mode is based around a client context driven use case.
Therefore, methods like Connect and Disconnect switch the current context of the shell and commands like Publish and
Subscribe always relate to the currently active client context.

```
mqtt shell
```

Alias: `mqtt sh`

## Example

```
$ mqtt shell                # starts the shell

mqtt> con -i myClient                # connect client with identifier 'myClient'
myClient@host> pub -t test -m msg    # publish the message 'msg' with the new context client
myClient@host> dis                   # disconnect and remove context
mqtt> ...
```

***

## Options

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

## Summary

### Commands **without** an active context

* [Connect](shell/connect.md)
* [Disconnect](shell/disconnect.md)
* [Switch](shell/switch.md)
* [List](shell/list.md)
* [Clear](shell/clear.md)
* [Exit](shell/exit.md)

### Commands **with** an active context

* [Publish](shell/publish.md)
* [Subscribe](shell/subscribe.md)
* [Unsubscribe](shell/unsubscribe.md)
* [Disconnect](shell/disconnect.md)
* [Switch](shell/switch.md)
* [List](shell/list.md)
* [Clear](shell/clear.md)
* [Exit](shell/exit.md)

**NOTE**: A client is uniquely identified in the CLI by the **hostname** and the unique **identifier**.
