---
nav_order: 1
redirect_from: /docs/hivemq/policies.html
---

# Policies

***

The policies command of the HiveMQ command line offers a set of commands to work with policies using a HiveMQ Data
Validation API endpoint.

```
mqtt hivemq policies
```

***

## Commands

| Command | Explanation                         |
|---------|-------------------------------------|
| get     | See [Get Policy](#get-policy)       |
| delete  | See [Delete Policy](#delete-policy) |
| create  | See [Create Policy](#create-policy) |
| list    | See [List Policies](#list-policies) |

***

## Options

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Get Policy

*** 

Retrieve a single existing policy.

```
mqtt hivemq policies get
```

***

## Simple Example

```
$ mqtt hivemq policies get --id my-policy-id 

{
  "createdAt": "2023-05-25T14:58:09.093Z",
  "id": "my-policy-id",
  "matching": {
    "topicFilter": "topic/1"
  },
  "onFailure": {
    "pipeline": []
  },
  "onSuccess": {
    "pipeline": []
  },
  "validation": {
    "validators": [
      {
        "arguments": {
          "strategy": "ALL_OF",
          "schemas": [
            {
              "schemaId": "my-schema"
            }
          ]
        },
        "type": "schema"
      }
    ]
  }
}
```

***

## Options

| Option | Long Version | Explanation                    | Default  |
|--------|--------------|--------------------------------|----------|
| `-i`   | `--id`       | The id of the policy to fetch. | required |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Delete Policy

*** 

Delete a single existing policy.

```
mqtt hivemq policies delete
```

***

## Simple Example

```
$ mqtt hivemq policies delete --id my-policy-id 
```

***

## Options

| Option | Long Version | Explanation                         | Default  |
|--------|--------------|-------------------------------------|----------|
| `-i`   | `--id`       | The id of the policy to be deleted. | required |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Create Policy

*** 

Create a new policy and upload it to a HiveMQ node.
The policy definition may be provided either directly from the command line or from a file.

```
mqtt hivemq policies create
```

***

## Simple Example

```
$ mqtt hivemq policies create --file my-policy.json 
```

***

## Options

| Option | Long Version   | Explanation                                                                                         | Default                                    |
|--------|----------------|-----------------------------------------------------------------------------------------------------|--------------------------------------------|
|        | `--definition` | The definition of the policy. This should be a JSON string containing a complete policy definition. | one of `--definition` or `--file` required |
|        | `--file`       | A path to a file containing the definition of the policy.                                           | one of `--definition` or `--file` required |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# List Policies

*** 

List every policy that exists on a HiveMQ node.
This list may optionally be filtered by policy id, schema id, and MQTT topic.


```
mqtt hivemq policies list
```

***

## Simple Example

```
$ mqtt hivemq policies list --topic topic/1

{
  "items": [
    {
      "createdAt": "2023-05-25T14:59:03.303Z",
      "id": "policy-1",
      "matching": {
        "topicFilter": "#"
      },
      "onFailure": {
        "pipeline": []
      },
      "onSuccess": {
        "pipeline": []
      },
      "validation": {
        "validators": [
          {
            "arguments": {
              "strategy": "ALL_OF",
              "schemas": [
                {
                  "schemaId": "schema-1"
                }
              ]
            },
            "type": "schema"
          }
        ]
      }
    },
    {
      "createdAt": "2023-05-25T15:18:09.093Z",
      "id": "policy-2",
      "matching": {
        "topicFilter": "topic/1"
      },
      "onFailure": {
        "pipeline": []
      },
      "onSuccess": {
        "pipeline": []
      },
      "validation": {
        "validators": [
          {
            "arguments": {
              "strategy": "ALL_OF",
              "schemas": [
                {
                  "schemaId": "schema-2"
                }
              ]
            },
            "type": "schema"
          }
        ]
      }
    }
  ]
}
```

***

## Options

| Option | Long Version  | Explanation                                                                                            | Default |
|--------|---------------|--------------------------------------------------------------------------------------------------------|---------|
| `-i`   | `--id`        | Filter by policies with a matching policy id. Can be specified multiple times.                         |         |
| `-s`   | `--schema-id` | Filter by policies that reference a schema with a matching schema id. Can be specified multiple times. |         |
| `-t`   | `--topic`     | Filter by policies that apply to a certain MQTT topic.                                                 |         |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}
