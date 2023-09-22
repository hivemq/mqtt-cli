---
nav_order: 1
redirect_from: /docs/hivemq/behavior-state.html
---

# Behavior State

***

The behavior-state command of the HiveMQ command line offers a set of commands to query the behavior validation state of
currently connected MQTT clients using a HiveMQ Data Hub API endpoint.

**NOTE**: The HiveMQ Data Hub is a new product in the HiveMQ platform and commands using it may be subject to breaking
changes in the future.

```
mqtt hivemq behavior-state
```

***

## Commands

| Command | Explanation                                   |
|---------|-----------------------------------------------|
| get     | See [Get Behavior State](#get-behavior-state) |

***

## Options

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Get Behavior State

*** 

Retrieve a list of behavior states for a connected MQTT client for every behavior policy which currently applies to that
client.

```
mqtt hivemq behavior-state get
```

***

## Simple Example

```
$ mqtt hivemq behavior-state get --id my-client-id
{
  "items": [
    {
      "arguments": {},
      "behaviorId": "Mqtt.events",
      "firstSetAt": "2023-09-21T11:54:35.111Z",
      "policyId": "matchingOne",
      "stateName": "Connected",
      "stateType": "INTERMEDIATE",
      "variables": {}
    }
  ]
}
```

***

## Options

| Option | Long Version | Explanation                    | Required |
|--------|--------------|--------------------------------|:--------:|
| `-i`   | `--id`       | The id of the client to fetch. |    X     |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}
ns.md %}
