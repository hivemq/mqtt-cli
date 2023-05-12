---
nav_order: 4
redirect_from: /docs/shell/subscribe.html
---

# Subscribe
***

The subscribe with a context subscribes the currently active context client to the given topics.
By default, it doesn't block the console like the [Subscribe](../subscribe.md) without a context does.
To enable this behavior you can use the **-s** option.


## Synopsis

```
client@host> sub    -t <topics> [-t <topics>]... 
                    [-q <qos>]... 
                    [-oc] 
                    [-of <receivedMessagesFile>]
                    [-b64]
                    [-J]
                    [-T] 
                    [-up <userProperties>]... 
                    [-s]   
                    [-h]                                           
```

***

##  Options

| Option | Long Version        | Explanation                                                                                                                                                                                                        | Default |
|--------|---------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| `-t`   | `--topic`           | The MQTT topic the client will subscribe to.                                                                                                                                                                       |         |
| `-q`   | `--qos`             | Use a defined quality of service level on all topics if only one QoS is specified. You can define a specific QoS level for every topic. The corresponding QoS levels will be matched in order to the given topics. | `0`     |
| `-oc`  | `--outputToConsole` | If this flag is set the output will be printed to the console.                                                                                                                                                     | `false` |
| `-of`  | `--outputToFile`    | If a file is given print the received publishes to the specified output file. If the file is not present it will be created.                                                                                       |         |
| `-b64` | `--base64`          | If set the received publish messages will be base64 encoded.                                                                                                                                                       | `false` |
| `-J`   | `--jsonOutput`      | Print the received publishes in pretty JSON format.                                                                                                                                                                | `False` |
| `-T`   | `--showTopics`      | Prepend the specific topic name to the received publish.                                                                                                                                                           | `False` |
| `-up`  | `--userProperty`    | A user property of the subscribe message.                                                                                                                                                                          |         |
| `-s`   |                     | The subscribe emulates the same behavior as the subscribe command in non-shell mode. <br> **Note**: the subscriptions will be unsubscribed afterwards. <br> To cancel the command simply press *Enter*.            |         |

## Examples

> Subscribe to test topic on default settings (output will be written to Logfile.
See [Logging](../logging.md)):

```
mqtt> con -i myClient
myClient@localhost> sub -t test
```
