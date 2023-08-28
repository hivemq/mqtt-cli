---
nav_order: 9
redirect_from: /docs/logging.html
---

# Logging

***

* All non-shell commands offer an `-l` option by which logging to a logfile under `~/.mqtt-cli/logs` can be activated
* The logfile and log level can be configured in the [MQTT-CLI configuration](configuration.md)
* By specifying the `-l` option for the `shell` command the whole shell-session will be logged to a logfile shown at
  start

***

## Direct Logging for Publish and Subscribe

If you require debug logging for direct access, the `-d` (debug) and `-v` (verbose) options are available for the basic
publish and subscribe command

```
$ mqtt pub -i c1 -t test -m "Hello World" -d -h broker.hivemq.com

Client 'c1@broker.hivemq.com' sending CONNECT
    MqttConnect{keepAlive=60, cleanStart=true, sessionExpiryInterval=0}
Client 'c1@broker.hivemq.com' received CONNACK
    MqttConnAck{reasonCode=SUCCESS, sessionPresent=false, restrictions=MqttConnAckRestrictions{receiveMaximum=10, maximumPacketSize=268435460, topicAliasMaximum=5, maximumQos=EXACTLY_ONCE, retainAvailable=true, wildcardSubscriptionAvailable=true, sharedSubscriptionAvailable=true, subscriptionIdentifiersAvailable=true}} 
Client 'c1@broker.hivemq.com' sending PUBLISH ('Hello World')
    MqttPublish{topic=test, payload=11byte, qos=AT_MOST_ONCE, retain=false}
Client 'c1@broker.hivemq.com' finish PUBLISH
    MqttPublishResult{publish=MqttPublish{topic=test, payload=11byte, qos=AT_MOST_ONCE, retain=false}}
```
