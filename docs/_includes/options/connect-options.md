| Option      | Long Version              | Explanation                                                                      | Default                            |
|-------------|---------------------------|----------------------------------------------------------------------------------|------------------------------------|
| `-h`        | `--host`                  | The MQTT host.                                                                   | `localhost`                        |
| `-p`        | `--port`                  | The MQTT port.                                                                   | `1883`                             |
| `-V`        | `--mqttVersion`           | The MQTT version can be set to 3 or 5.                                           | `5`                                |
| `-i`        | `--identifier`            | A unique client identifier can be defined.                                       | A randomly generated UTF-8 String. |
| `-ip`       | `--identifierPrefix`      | The prefix for randomly generated client identifiers, if no identifier is given. | `mqttClient`                       |
| `-c`        | `--[no-]cleanStart`       | Whether the client should start a clean session.                                 | `true`                             |
| `k`         | `--keepAlive`             | The keep alive of the client (in seconds).                                       | `60`                               |
| `-se`       | `--sessionExpiryInterval` | The session expiry value in seconds.                                             | `0` (Instant Expiry)               |
| `-Cup`      | `--connectUserProperty`   | A user property of the connect message.                                          |                                    |
| `-ws`       |                           | Use WebSocket transport protocol.                                                | `false`                            |
| `-ws:path`  |                           | The path to the WebSocket located at the given broker host.                      |                                    |
