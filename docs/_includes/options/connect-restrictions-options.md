| Option | Long Version            | Explanation                                                                                                       | Default     |
|--------|-------------------------|-------------------------------------------------------------------------------------------------------------------|-------------|
|        | `--rcvMax`              | The maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts from the server concurrently. | `65535`     |
|        | `--sendMax`             | The maximum amount of not acknowledged publishes with QoS 1 or 2 the client sends to the server concurrently.     | `65535`     |
|        | `--maxPacketSize`       | The maximum packet size the client accepts from the server.                                                       | `268435460` |
|        | `--sendMaxPacketSize`   | The maximum packet size the client sends to the server.                                                           | `268435460` |
|        | `--topicAliasMax`       | The maximum amount of topic aliases the client accepts from the server.                                           | `0`         |
|        | `--sendTopicAliasMax`   | The maximum amount of topic aliases the client sends to the server.                                               | `16`        |
|        | `--[no-]reqProblemInfo` | The client requests problem information from the server.                                                          | `true`      |
|        | `--reqResponseInfo`     | The client requests response information from the server.                                                         | `false`     |
