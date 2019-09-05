---
layout: default
title: Configuration
nav_order: 3
has_children: false
---

## Default Properties

HiveMQ-CLI uses some default values which can be overwritten.
HiveMQ-CLI stores the default values in a properties file which is located under the user home directory of your OS under `~/.hivemq-cli/config.properties`.

On the first successful execution of the CLI this file will be generated and will look like the following example:

```
mqtt.port=1883
client.prefix=hmqClient
mqtt.host=localhost
mqtt.version=5
debug.level.shell=VERBOSE
debug.logfile.path=/Users/tseeberg/.hivemq-cli/logs/
```

A properties file lists all the properties as key-value pairs.
Therefore you have to specify the values to the following keys if you want to overwrite the given default values.


|Key      | Explanation    | Default |
| ------- | -------------- | ------------------------- | 
| ``mqtt.host``   | The default host which the client will connect to.| ``localhost``
| ``mqtt.port``   | The default port which the client will connect to.| ``1883``
| ``mqtt.version``| The default mqtt version with which the client will be connected. | ``5``
| ``client.prefix`` | The default client prefix which will be prepended to the randomly generated client name if no identifier is given | ``hmqClient``
| ``client.subscribe.output``| The default filepath to which all the received publishes of a subscribed client will be written to See `sub -of` option |
| ``debug.level.shell``| The default debug level of the shell which may be one of the following values: ``{INFO \| DEBUG \| VERBOSE}`` | ``VERBOSE``
| ``debug.logfile.path`` | The default path to the logfile directory to which all the logs will be written | `~/.hivemq-cli/logs`

