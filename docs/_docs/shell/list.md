---
nav_order: 6
redirect_from: /docs/shell/list.html
---

# List

***

Lists all the connected clients of this mqtt-cli shell session.

```
mqtt> list
```

Alias: `mqtt> ls`

***

## Options

| Option | Long Version      | Explanation                                                            | Default |
|--------|-------------------|------------------------------------------------------------------------|---------|
| `-l`   | `--long`          | Use a long listing format with detailed information about the clients. | `false` |
| `-r`   | `--reverse`       | Reverse order while sorting.                                           | `false` |
| `-s`   | `--subscriptions` | List subscribed topics of the clients.                                 | `false` |
| `-t`   | `--time`          | Sort clients by their creation time.                                   | `false` |
| `-U`   |                   | Do not sort.                                                           | `false` |

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

## Examples

Connect two clients and list them by default settings

```
mqtt> con -i client1
client1@localhost> exit
mqtt> con -i client2
client2@localhost> ls
client1@localhost
client2@localhost
```

***

Connect a client and show detailed information about it

```
mqtt> con -i client
client@localhost> ls -l
total 1
CONNECTED    11:00:29 client1 localhost  1883 MQTT_5_0 NO_SSL
```

***

List subscriptions of all connected clients

``` 
client1@localhost> sub -t topic -t topic2 -t topic3
client1@localhost> ls -s
client1@localhost
 -subscribed topics: [topic2, topic3, topic]
```
