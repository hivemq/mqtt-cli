---
nav_order: 1
redirect_from: /docs/hivemq/behavior-policy.html
---

# Behavior Policy

***

The behavior-policy command of the HiveMQ command line offers a set of commands to work with behavior validation
policies using a HiveMQ Data Hub API endpoint.

**NOTE**: The HiveMQ Data Hub is a new product in the HiveMQ platform and commands using it may be subject to breaking
changes in the future.

```
mqtt hivemq behavior-policy
```

***

## Commands

| Command | Explanation                                           |
|---------|-------------------------------------------------------|
| create  | See [Create Behavior Policy](#create-behavior-policy) |
| get     | See [Get Behavior Policy](#get-behavior-policy)       |
| update  | See [Update Behavior Policy](#update-behavior-policy) |
| list    | See [List Behavior Policies](#list-behavior-policies) |
| delete  | See [Delete Behavior Policy](#delete-behavior-policy) |

***

## Options

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Create Behavior Policy

*** 

Create a new behavior policy and upload it to a HiveMQ node.
The policy definition may be provided either directly from the command line or from a file.

```
mqtt hivemq behavior-policy create
```

***

## Simple Example

```
mqtt hivemq behavior-policy create --file my-policy.json 
```

***

## Options

| Option | Long Version   | Explanation                                                                                                  |                    Required                     |
|--------|----------------|--------------------------------------------------------------------------------------------------------------|:-----------------------------------------------:|
|        | `--definition` | The definition of the behavior policy. This should be a JSON string containing a complete policy definition. | Either `--definition` or `--file`, but not both |
|        | `--file`       | A path to a file containing the definition of the behavior policy.                                           | Either `--definition` or `--file`, but not both |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Get Behavior Policy

*** 

Retrieve a single existing behavior policy.

```
mqtt hivemq behavior-policy get
```

***

## Simple Example

```
$ mqtt hivemq behavior-policy get --id matchingThree 
{
  "id": "matchingThree",
  "createdAt": "2023-09-21T11:24:25.464Z",
  "lastUpdatedAt": "2023-09-21T11:24:25.464Z",
  "matching": {
    "clientIdRegex": "three.*"
  },
  "deserialization": {
    "publish": {
      "schema": {
        "schemaId": "one",
        "version": "latest"
      }
    },
    "will": {
      "schema": {
        "schemaId": "one",
        "version": "latest"
      }
    }
  },
  "behavior": {
    "id": "Mqtt.events",
    "arguments": {}
  },
  "onTransitions": []
}
```

***

## Options

| Option | Long Version | Explanation                                                                       | Required |
|--------|--------------|-----------------------------------------------------------------------------------|:--------:|
| `-i`   | `--id`       | The id of the policy to fetch.                                                    |    X     |
| `-f`   | `--field`    | Select which JSON fields appear in the response. Can be specified multiple times. |          |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Update Behavior Policy

*** 

Update an existing behavior policy.
The policy definition may be provided either directly from the command line or from a file.
The provided policy id argument must match the id in the policy definition.

```
mqtt hivemq behavior-policy update
```

***

## Simple Example

```
mqtt hivemq behavior-policy update --id my-policy-id --file behavior-one.json 
```

***

## Options

| Option | Long Version   | Explanation                                                                                                  |                    Required                     |
|--------|----------------|--------------------------------------------------------------------------------------------------------------|:-----------------------------------------------:|
| `-i`   | `--id`         | The id of the behavior policy to be updated.                                                                 |                        X                        |
|        | `--definition` | The definition of the behavior policy. This should be a JSON string containing a complete policy definition. | Either `--definition` or `--file`, but not both |
|        | `--file`       | A path to a file containing the definition of the behavior policy.                                           | Either `--definition` or `--file`, but not both |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}


***

# List Behavior Policies

*** 

List every behavior policy that exists on a HiveMQ node.
This list may optionally be filtered by policy id and matching client id.

```
mqtt hivemq behavior-policy list
```

***

## Simple Example

```
$ mqtt hivemq behavior-policy list -i behaviorOne -i behaviorTwo
{
  "items": [
    {
      "id": "behaviorOne",
      "createdAt": "2023-09-19T23:52:28.381Z",
      "lastUpdatedAt": "2023-09-19T23:52:28.381Z",
      "matching": {
        "clientIdRegex": "one.*"
      },
      "behavior": {
        "id": "Mqtt.events",
        "arguments": {}
      },
      "onTransitions": []
    },
    {
      "id": "behaviorTwo",
      "createdAt": "2023-09-21T11:24:25.464Z",
      "lastUpdatedAt": "2023-09-21T11:24:25.464Z",
      "matching": {
        "clientIdRegex": "two.*"
      },
      "deserialization": {
        "publish": {
          "schema": {
            "schemaId": "schema1",
            "version": "latest"
          }
        }
      },
      "behavior": {
        "id": "Mqtt.events",
        "arguments": {}
      },
      "onTransitions": [
        {
          "Connection.OnDisconnect": {
            "pipeline": [
              {
                "arguments": {
                  "metricName": "testMetric",
                  "incrementBy": 1
                },
                "functionId": "Metrics.Counter.increment",
                "id": "increment1"
              }
            ]
          },
          "fromState": "any.*",
          "toState": "any.*"
        }
      ]
    }
  ]
}
```

***

## Options

| Option | Long Version  | Explanation                                                                                                   |
|--------|---------------|---------------------------------------------------------------------------------------------------------------|
| `-i`   | `--id`        | Filter by behavior policies with a matching policy id. Can be specified multiple times.                       |          
| `-c`   | `--client-id` | Filter by behavior policies that apply to clients with a matching client id. Can be specified multiple times. |          
| `-f`   | `--field`     | Select which JSON fields appear in the response. Can be specified multiple times.                             |          
|        | `--limit`     | Limit the maximum number of policies returned.                                                                |          

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Delete Behavior Policy

*** 

Delete a single existing behavior policy.

```
mqtt hivemq behavior-policy delete
```

***

## Simple Example

```
mqtt hivemq behavior-policy delete --id my-policy-id 
```

***

## Options

| Option | Long Version | Explanation                                  | Required |
|--------|--------------|----------------------------------------------|:--------:|
| `-i`   | `--id`       | The id of the behavior policy to be deleted. |    X     |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}
