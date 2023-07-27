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
| create  | See [Create Policy](#create-policy) |
| get     | See [Get Policy](#get-policy)       |
| update  | See [Update Policy](#update-policy) |
| list    | See [List Policies](#list-policies) |
| delete  | See [Delete Policy](#delete-policy) |

***

## Options

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
mqtt hivemq policies create --file my-policy.json 
```

***

## Options

| Option | Long Version   | Explanation                                                                                         |                    Required                     |
|--------|----------------|-----------------------------------------------------------------------------------------------------|:-----------------------------------------------:|
|        | `--definition` | The definition of the policy. This should be a JSON string containing a complete policy definition. | Either `--definition` or `--file`, but not both |
|        | `--file`       | A path to a file containing the definition of the policy.                                           | Either `--definition` or `--file`, but not both |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

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

# Update Policy

*** 

Update an existing policy.
The policy definition may be provided either directly from the command line or from a file.
The provided policy ID argument must match the ID in the policy definition.

```
mqtt hivemq policies update
```

***

## Simple Example

```
mqtt hivemq policies update --id my-policy-id --file my-policy.json 
```

***

## Options

| Option | Long Version   | Explanation                                                                                         |                    Required                     |
|--------|----------------|-----------------------------------------------------------------------------------------------------|:-----------------------------------------------:|
| `-i`   | `--id`         | The id of the policy to be updated.                                                                 |                        X                        |
|        | `--definition` | The definition of the policy. This should be a JSON string containing a complete policy definition. | Either `--definition` or `--file`, but not both |
|        | `--file`       | A path to a file containing the definition of the policy.                                           | Either `--definition` or `--file`, but not both |

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

| Option | Long Version  | Explanation                                                                                            | Required |
|--------|---------------|--------------------------------------------------------------------------------------------------------|:--------:|
| `-i`   | `--id`        | Filter by policies with a matching policy id. Can be specified multiple times.                         |          |
| `-s`   | `--schema-id` | Filter by policies that reference a schema with a matching schema id. Can be specified multiple times. |          |
| `-t`   | `--topic`     | Filter by policies that apply to a certain MQTT topic.                                                 |          |
| `-f`   | `--field`     | Select which JSON fields appear in the response. Can be specified multiple times.                      |          |
|        | `--limit`     | Limit the maximum number of policies returned.                                                         |          |

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
mqtt hivemq policies delete --id my-policy-id 
```

***

## Options

| Option | Long Version | Explanation                         | Required |
|--------|--------------|-------------------------------------|:--------:|
| `-i`   | `--id`       | The id of the policy to be deleted. |    X     |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}
