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

***

## Example

```
$ mqtt shell                # starts the shell

mqtt> con -i myClient                # connect client with identifier 'myClient'
myClient@host> pub -t test -m msg    # publish the message 'msg' with the new context client
myClient@host> dis                   # disconnect and remove context
mqtt> ...
```

**NOTE**: A client is uniquely identified in the CLI by the **hostname** and the unique **identifier**.

***

## Commands

| Command     | Explanation                             | Requires active client context |
|-------------|-----------------------------------------|:------------------------------:|
| connect     | See [Connect](shell/connect.md)         |                                |
| disconnect  | See [Disconnect](shell/disconnect.md)   |                                |
| publish     | See [Publish](shell/publish.md)         |               X                |
| subscribe   | See [Subscribe](shell/subscribe.md)     |               X                |
| unsubscribe | See [Unsubscribe](shell/unsubscribe.md) |               X                |
| switch      | See [Switch](shell/switch.md)           |                                |
| list        | See [List](shell/list.md)               |                                |
| clear       | See [Clear](shell/clear.md)             |                                |
| exit        | See [Exit](shell/exit.md)               |                                |

***

## Options

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}
