---
nav_order: 2
redirect_from: /docs/shell/disconnect.html
---

# Disconnect

***

Disconnects a previously connected client.

```
mqtt> disconnect
```

Alias: `mqtt> dis`

***

Disconnects the currently active client context.

```
client@host> disconnect
```

Alias: `client@host> dis`

***

## Options

| Option | Long Version      | Explanation                                | Default         |
|--------|-------------------|--------------------------------------------|-----------------|
| `-a`   | `--all`           | Disconnect all connected clients.          |                 |
| `-i`   | `--identifier`    | The unique identifier of a client.         |                 |
| `-h`   | `--host`          | The host the client is connected to.       | `localhost`     |
| `-e`   | `--sessionExpiry` | Session expiry value in seconds.           | `0` (No Expiry) |
| `-r`   | `--reason`        | Reason string for the disconnect.          |                 |
| `-up`  | `--userProperty`  | A user property of the disconnect message. |                 |

### Help Options

{% include options/help-options.md defaultHelp=false %}

***

## Examples

Connect a client which is identified by myClient and disconnect it afterward using default settings

```
mqtt> con -i myClient
myClient@localhost> dis
mqtt>
```

***

Connect a client which is identified by myClient on specific settings and disconnect it afterward

```
mqtt> con -i myClient -h broker.hivemq.com
myClient@localhost> exit  # client is still connected
mqtt> dis -i myClient -h broker.hivemq.com
```

**NOTE**: When specifying the **identifier** in order to uniquely identify the desired client, the **hostname** must
also be provided.
If you don't specify these the default settings for these attributes will be used which may lead to unexpected behavior.
