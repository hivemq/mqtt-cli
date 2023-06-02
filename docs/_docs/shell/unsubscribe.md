---
nav_order: 5
redirect_from: /docs/shell/unsubscribe.html
---

# Unsubscribe

***

Unsubscribes the currently active context client from a list of topics
and is therefore only available in Shell mode.

```
client@host> unsubscribe
```

Alias: `client@host> unsub`

***

## Options

| Option | Long Version     | Explanation                                          |
|--------|------------------|------------------------------------------------------|
| `-t`   | `--topic`        | A topic from which the client will unsubscribe from. |
| `-up`  | `--userProperty` | A user property of the unsubscribe message.          |

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

## Example

Connect a client which is identified by myClient and subscribe it to two topics afterward. 
Then unsubscribe from one of the two topics:

```
mqtt> con -i myClient
myClient@localhost> sub -t topic1 -t topic2
myClient@localhost> unsub -t topic1
mqtt>
```
