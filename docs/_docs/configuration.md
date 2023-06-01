---
title: Configuration
nav_order: 10
redirect_from: /docs/configuration.html
---

{% comment %}disable jekyll-titles-from-headings{% endcomment %}

# Default Properties

***

MQTT CLI uses some default values which can be overwritten.
MQTT CLI stores the default values in a properties file which is located under the user home directory of your OS
under `$HOME/.mqtt-cli/config.properties`.

On the first successful execution of the CLI this file will be generated and will look like the following example:

```
mqtt.host=localhost
mqtt.port=1883
mqtt.version=5
client.id.prefix=mqtt
ws.path=/mqtt
client.id.length=8
logfile.level=debug
logfile.path=/Users/tseeberg/.mqtt-cli/logs/
```

A properties file lists all the properties as key-value pairs.
Therefore, you have to specify the values to the following keys if you want to overwrite the given default values.

| Key                                 | Explanation                                                                                                       | Default            |
|-------------------------------------|-------------------------------------------------------------------------------------------------------------------|--------------------| 
| `mqtt.host`                         | The address of the broker which the client will connect to.                                                       | `localhost`        |
| `mqtt.port`                         | The port of the broker which the client will connect to.                                                          | `1883`             |
| `mqtt.version`                      | The mqtt version which the client will use.                                                                       | `5`                |
| `client.id.prefix`                  | The client prefix which will be prepended to the randomly generated client names.                                 | `mqtt`             |
| `client.id.length`                  | The length of the randomly generated client id (if a client id is not provided).                                  | `8`                |
| `ws.path`                           | The WebSocket path on a broker.                                                                                   | `/mqtt`            |
| `auth.username`                     | The username to use for authentication.                                                                           |                    |
| `auth.password`                     | The password to use for authentication.                                                                           |                    |
| `auth.password.env`                 | The environment variable to read the password from.                                                               |                    |
| `auth.password.file`                | The file to read the password from.                                                                               |                    |
| `auth.client.cert`                  | The path to the client certificate.                                                                               |                    |
| `auth.client.key`                   | The path to the client key corresponding to the certificate.                                                      |                    |
| `auth.server.cafile`                | The path to the server certificate.                                                                               |                    |
| `auth.keystore`                     | The path to the client keystore for client side authentication.                                                   |                    |
| `auth.keystore.password`            | The password for the keystore.                                                                                    |                    |
| `auth.keystore.privatekey.password` | The password for the private key inside the keystore.                                                             |                    |
| `auth.truststore`                   | The path to the client truststore to enable encrypted certificate based communication.                            |                    |
| `auth.truststore.password`          | The password for the truststore.                                                                                  |                    |
| `client.subscribe.output`           | The filepath to which all the received publishes of a subscribed client will be written to. See `sub -of` option. |                    |
| `logfile.level`                     | The debug level for the logfile which may be one of the following values: `{INFO \| DEBUG \| TRACE}`              | `debug`            |
| `logfile.path`                      | The path to the logfile directory to which all the logs will be written.                                          | `~/.mqtt-cli/logs` |
