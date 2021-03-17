---
layout: default
title: Configuration
nav_order: 10
has_children: false
---

{:.main-header-color-yellow}
# Default Properties
***

MQTT CLI uses some default values which can be overwritten.
MQTT CLI stores the default values in a properties file which is located under the user home directory of your OS under `$HOME/.mqtt-cli/config.properties`.

On the first successful execution of the CLI this file will be generated and will look like the following example:

```
mqtt.host=localhost
mqtt.port=1883
mqtt.version=5
client.id.prefix=mqtt
ws.path=/mqtt
client.id.length=8
logfile.level=verbose
logfile.path=/Users/tseeberg/.mqtt-cli/logs/
```

A properties file lists all the properties as key-value pairs.
Therefore you have to specify the values to the following keys if you want to overwrite the given default values.


|Key      | Explanation    | Default |
| ------- | -------------- | ------------------------- | 
| ``mqtt.host``   | The default address of the broker which the client will connect to.| ``localhost``
| ``mqtt.port``   | The default port of the broker which the client will connect to.| ``1883``
| ``mqtt.version``| The default mqtt version witch client will use. | ``5``
| ``client.id.prefix`` | The default client prefix which will be prepended to the randomly generated client names. | ``mqttClient``
| ``client.id.length`` | The length of the randomly generated client id if none is given | ``8``
| ``ws.path`` | The default WebSocket path on a broker | ``/mqtt``
| ``auth.username`` | The default username to use for authentication |
| ``auth.password`` | The default password to use for authentication |
| ``auth.password.env`` | The default environment variable to read the password from |
| ``auth.password.file`` | The default file to read the password from |
| ``auth.client.cert`` | The path to the default client certificate  | 
| ``auth.client.key`` | The path to the default client key corresponding to the certificate  |
| ``auth.server.cafile`` | The path to the default server certificate  |
| ``client.subscribe.output``| The default filepath to which all the received publishes of a subscribed client will be written to. See `sub -of` option |
| ``logfile.level``| The default debug level for the logfile which may be one of the following values: ``{INFO | DEBUG | TRACE}`` | ``DEBUG``
| ``logfile.path`` | The default path to the logfile directory to which all the logs will be written | `~/.mqtt-cli/logs`

