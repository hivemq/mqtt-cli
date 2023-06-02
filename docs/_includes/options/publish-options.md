| Option     | Long Version               | Explanation                                                                                                                                                                                                                           | Default |
|------------|----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| `-t`       | `--topic`                  | The MQTT topic to which the message will be published.                                                                                                                                                                                |         |
| `-m`       | `--message`                | The message which will be published on the topic.                                                                                                                                                                                     |         |
| `-m:file`  | `--message-file`           | The file containing the payload which will be published on the topic.                                                                                                                                                                 |         |
| `-m:empty` | `--message-empty`          | Sets the message to an empty payload.                                                                                                                                                                                                 |         |
| `-r`       | `--[no-]retain`            | Whether the message will be retained.                                                                                                                                                                                                 | `false` |
| `-q`       | `--qos`                    | Define the quality of service level. If only one QoS is specified it will be used for all topics.<br> You can define a specific QoS level for every topic. The corresponding QoS levels will be matched in order to the given topics. | `0`     |
| `-e`       | `--messageExpiryInterval`  | The lifetime of the publish message in seconds.                                                                                                                                                                                       |         |
| `-ct`      | `--contentType`            | A description of the content of the publish message.                                                                                                                                                                                  |         |
| `-cd`      | `--correlationData`        | The correlation data of the publish message.                                                                                                                                                                                          |         |
| `-pf`      | `--payloadFormatIndicator` | The payload format indicator of the publish message.                                                                                                                                                                                  |         |
| `-rt`      | `--responseTopic`          | The topic name for the response message of the publish message.                                                                                                                                                                       |         |
| `-up`      | `--userProperty`           | A user property of the publish message.                                                                                                                                                                                               |         |