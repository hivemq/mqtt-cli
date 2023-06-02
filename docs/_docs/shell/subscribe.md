---
nav_order: 4
redirect_from: /docs/shell/subscribe.html
---

# Subscribe

***

The subscribe with a context subscribes the currently active context client to the given topics.
By default, it doesn't block the console like the [Subscribe](../subscribe.md) without a context does.
To enable this behavior you can use the **-s** option.

```
client@host> subscribe
```

Alias: `client@host> sub`

***

## Options

| Option | Long Version        | Explanation                                                                                                                                                                                      | Default |
|--------|---------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| `-oc`  | `--outputToConsole` | If this flag is set the output will be printed to the console.                                                                                                                                   | `false` |
| `-s`   | `--stay`            | The subscribe emulates the same behavior as the subscribe command in non-shell mode. <br> **NOTE**: the subscriptions will be unsubscribed afterwards. <br> To cancel the command press *Enter*. | `false` |

### Subscribe Options

{% include options/subscribe-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

## Examples

Subscribe to test topic on default settings (output will be written to Logfile. See [Logging](../logging.md)):

```
mqtt> con -i myClient
myClient@localhost> sub -t test
```
