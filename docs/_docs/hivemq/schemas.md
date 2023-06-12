---
nav_order: 1
redirect_from: /docs/hivemq/schemas.html
---

# Schemas

***

The schemas command of the HiveMQ command line offers a set of commands to work with schemas using a HiveMQ Data
Validation API endpoint.

```
mqtt hivemq schemas
```

***

## Commands

| Command | Explanation                         |
|---------|-------------------------------------|
| create  | See [Create Schema](#create-schema) |
| get     | See [Get Schema](#get-schema)       |
| delete  | See [Delete Schema](#delete-schema) |

***

## Options

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Create Schema

*** 

Create a new schema and upload it to a HiveMQ node.
The schema definition may be provided either directly from the command line or from a file.

```
mqtt hivemq schemas create
```

***

## Simple Example

```
mqtt hivemq schemas create --id my-schema-id --type PROTOBUF --file my-schema.desc --message-type Message 
```

***

## Options

| Option | Long Version      | Explanation                                                                                                                                  | Default |                    Required                     |
|--------|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------|---------|:-----------------------------------------------:|
| `-i`   | `--id`            | The id of the schema to be created.                                                                                                          |         |                        X                        |
|        | `--type`          | The type of the schema. (Currently supported schema types [`JSON`, `PROTOBUF`])                                                              |         |                        X                        |
|        | `--definition`    | The definition of the schema. This should be a JSON Schema string for '--type JSON' or a compiled Protobuf descriptor for '--type PROTOBUF'. |         | Either `--definition` or `--file`, but not both |
|        | `--file`          | A path to a file containing the definition of the schema.                                                                                    |         | Either `--definition` or `--file`, but not both |
|        | `--message-type`  | Only used with '--type PROTOBUF'. The Protobuf message type to use for the schema.                                                           |         |            When `type` is `PROTOBUF`            |
|        | `--allow-unknown` | Only used with '--type PROTOBUF'. If provided, Protobuf messages may contain fields not specified in the schema.                             | `false` |                                                 |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Get Schema

*** 

Retrieve a single existing schema.

```
mqtt hivemq schemas get
```

***

## Simple Example

```
$ mqtt hivemq schemas get --id my-schema-id 
{
  "arguments": {},
  "createdAt": "2023-05-25T21:10:42.779Z",
  "id": "my-schema-id",
  "schemaDefinition": "eyAidHlwZSI6ICJvYmplY3QiIH0K",
  "type": "JSON"
}
```

***

## Options

| Option | Long Version | Explanation                    | Required |
|--------|--------------|--------------------------------|:--------:|
| `-i`   | `--id`       | The id of the schema to fetch. |    X     |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Delete Schema

*** 

Delete a single existing schema.

```
mqtt hivemq schemas delete
```

***

## Simple Example

```
mqtt hivemq schemas delete --id my-schema-id 
```

***

## Options

| Option | Long Version | Explanation                         | Required |
|--------|--------------|-------------------------------------|:--------:|
| `-i`   | `--id`       | The id of the schema to be deleted. |    X     |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}
