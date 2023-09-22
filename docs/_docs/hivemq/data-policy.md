---
nav_order: 1
redirect_from:
    - /docs/hivemq/policies.html
    - /docs/hivemq/data-policy.html
---

# Data Policy

***

The data-policy command of the HiveMQ command line offers a set of commands to work with data validation policies using
a HiveMQ Data Hub API endpoint.

**NOTE**: The HiveMQ Data Hub is a new product in the HiveMQ platform and commands using it may be subject to breaking
changes in the future.

```
mqtt hivemq data-policy
```

***

## Commands

| Command | Explanation                                   |
|---------|-----------------------------------------------|
| create  | See [Create Data Policy](#create-data-policy) |
| get     | See [Get Data Policy](#get-data-policy)       |
| update  | See [Update Data Policy](#update-data-policy) |
| list    | See [List Data Policies](#list-data-policies) |
| delete  | See [Delete Data Policy](#delete-data-policy) |

***

## Options

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Create Data Policy

*** 

Create a new data policy and upload it to a HiveMQ node.
The policy definition may be provided either directly from the command line or from a file.

```
mqtt hivemq data-policy create
```

***

## Simple Example

```
mqtt hivemq data-policy create --file my-policy.json 
```

***

## Options

| Option | Long Version   | Explanation                                                                                              |                    Required                     |
|--------|----------------|----------------------------------------------------------------------------------------------------------|:-----------------------------------------------:|
|        | `--definition` | The definition of the data policy. This should be a JSON string containing a complete policy definition. | Either `--definition` or `--file`, but not both |
|        | `--file`       | A path to a file containing the definition of the policy.                                                | Either `--definition` or `--file`, but not both |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Get Data Policy

*** 

Retrieve a single existing policy.

```
mqtt hivemq data-policy get
```

***

## Simple Example

```
$ mqtt hivemq data-policy get --id my-policy-id 
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
| `-i`   | `--id`       | The id of the data policy to fetch.                                               |    X     |
| `-f`   | `--field`    | Select which JSON fields appear in the response. Can be specified multiple times. |          |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Update Data Policy

*** 

Update an existing data policy.
The policy definition may be provided either directly from the command line or from a file.
The provided policy id argument must match the id in the policy definition.

```
mqtt hivemq data-policy update
```

***

## Simple Example

```
mqtt hivemq data-policy update --id my-policy-id --file my-policy.json 
```

***

## Options

| Option | Long Version   | Explanation                                                                                              |                    Required                     |
|--------|----------------|----------------------------------------------------------------------------------------------------------|:-----------------------------------------------:|
| `-i`   | `--id`         | The id of the data policy to be updated.                                                                 |                        X                        |
|        | `--definition` | The definition of the data policy. This should be a JSON string containing a complete policy definition. | Either `--definition` or `--file`, but not both |
|        | `--file`       | A path to a file containing the definition of the data policy.                                           | Either `--definition` or `--file`, but not both |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}


***

# List Data Policies

*** 

List every data policy that exists on a HiveMQ node.
This list may optionally be filtered by policy id, schema id, and MQTT topic.

```
mqtt hivemq data-policy list
```

***

## Simple Example

```
$ mqtt hivemq data-policy list --topic topic/1
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

# Delete Data Policy

*** 

Delete a single existing data policy.

```
mqtt hivemq data-policy delete
```

***

## Simple Example

```
mqtt hivemq data-policy delete --id my-policy-id 
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
