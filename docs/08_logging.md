---
layout: default
title: Logging
nav_order: 8
---

{:.main-header-color-yellow}
# Logging
***

* By starting MQTT CLI in Shell-Mode a log file will be created and all trace level information will be logged to the file.
* Verbose mode allows you to get more information about each MQTT message and the parameters.
* By default the Shell-Mode logs all commands in verbose mode to a uniquely named logfile which is placed in ``~.mqtt-cli/logs`` which is printed out at the start of the shell.

```
$ mqtt shell 

....
Press Ctl-C to exit.

Writing Logfile to /~/.mqtt-cli/logs/hmq-cli.<Year-Month-Day>.log
mqtt>
```



## Usage Example
> If you require debug logging for direct access add the `-d` and `-v` options are available for the basic publish and subscribe command


```
$ mqtt pub -i c1 -t test -m "Hello World" -d -h broker.hivemq.com

Client 'c1@broker.hivemq.com' sending CONNECT MqttConnect{keepAlive=60, cleanStart=true, sessionExpiryInterval=0}
Client 'c1@broker.hivemq.com' received CONNACK MqttConnAck{reasonCode=SUCCESS, sessionPresent=false, restrictions=MqttConnAckRestrictions{receiveMaximum=10, maximumPacketSize=268435460, topicAliasMaximum=5, maximumQos=EXACTLY_ONCE, retainAvailable=true, wildcardSubscriptionAvailable=true, sharedSubscriptionAvailable=true, subscriptionIdentifiersAvailable=true}} 
Client 'c1@broker.hivemq.com' sending PUBLISH ('Hello World') MqttPublish{topic=test, payload=11byte, qos=AT_MOST_ONCE, retain=false}
Client 'c1@broker.hivemq.com' received PUBLISH acknowledgement MqttPublishResult{publish=MqttPublish{topic=test, payload=11byte, qos=AT_MOST_ONCE, retain=false}}

```
