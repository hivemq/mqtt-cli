---
layout: default
title: Logging
nav_order: 8
---

{:.main-header-color-yellow}
# Logging
***
* All non-shell commands offer an ``-l`` option by which logging to a logfile under ``~/.mqtt-cli/logs`` can be activated
* The logfile and loglevel can be configured in the [MQTT-CLI configuration](configuration)
* By specifying the ``-l`` option for the ``shell`` command the whole shell-session will be logged to a logfile shown at start

## Direct logging for publish & subscribe
> If you require debug logging for direct access add the `-d` and `-v` options are available for the basic publish and subscribe command


```
$ mqtt pub -i c1 -t test -m "Hello World" -d -h broker.hivemq.com

Client 'c1@broker.hivemq.com' sending CONNECT MqttConnect{keepAlive=60, cleanStart=true, sessionExpiryInterval=0}
Client 'c1@broker.hivemq.com' received CONNACK MqttConnAck{reasonCode=SUCCESS, sessionPresent=false, restrictions=MqttConnAckRestrictions{receiveMaximum=10, maximumPacketSize=268435460, topicAliasMaximum=5, maximumQos=EXACTLY_ONCE, retainAvailable=true, wildcardSubscriptionAvailable=true, sharedSubscriptionAvailable=true, subscriptionIdentifiersAvailable=true}} 
Client 'c1@broker.hivemq.com' sending PUBLISH ('Hello World') MqttPublish{topic=test, payload=11byte, qos=AT_MOST_ONCE, retain=false}
Client 'c1@broker.hivemq.com' received PUBLISH acknowledgement MqttPublishResult{publish=MqttPublish{topic=test, payload=11byte, qos=AT_MOST_ONCE, retain=false}}

```
