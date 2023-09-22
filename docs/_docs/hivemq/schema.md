---
nav_order: 1
redirect_from: 
    - /docs/hivemq/schemas.html
     -/docs/hivemq/schema.html
---

# Schemas

***

The schema command of the HiveMQ command line offers a set of commands to work with schemas using a HiveMQ Data Hub API
endpoint.

**NOTE**: The HiveMQ Data Hub is a new product in the HiveMQ platform and commands using it may be subject to breaking
changes in the future.

```
mqtt hivemq schema
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
mqtt hivemq schema create
```

***

## Simple Example

```
mqtt hivemq schema create --id my-schema-id --type PROTOBUF --file my-schema.desc --message-type Message 
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
|        | `--print-version` | Print the assigned schema version after creation.                                                                                            | `false` |                                                 |

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
mqtt hivemq schema get
```

***

## Simple Example

```
$ mqtt hivemq schema get --id my-schema-id 
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

| Option | Long Version | Explanation                                                                       | Required |
|--------|--------------|-----------------------------------------------------------------------------------|:--------:|
| `-i`   | `--id`       | The id of the schema to fetch.                                                    |    X     |
| `-f`   | `--field`    | Select which JSON fields appear in the response. Can be specified multiple times. |          |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# List Schemas

*** 

List every schema that exists on a HiveMQ node.
This list may optionally be filtered by schema id and schema type.

```
mqtt hivemq schema list
```

***

## Simple Example

```
$ mqtt hivemq schema list --id schema-1
{
  "items": [
    {
      "id": "schema-1",
      "version": 1,
      "createdAt": "2023-06-12T00:38:34.911Z",
      "type": "JSON",
      "schemaDefinition": "ewogICJkZXNjcmlwdGlvbiI6ICJUaGlzIGlzIGFub3RoZXIgZ2VuZXJpYyBKU09OIHNjaGVtYSwgc2luY2UgaXQgcmVxdWlyZXMganVzdCBhIEpTT04sIG5vdGhpbmcgZnVydGhlciBzcGVjaWZpZWQiLAogICJ0eXBlIjogIm9iamVjdCIKfQo",
      "arguments": {}
    },
    {
      "id": "schema-1",
      "version": 2,
      "createdAt": "2023-06-12T09:57:55.862Z",
      "type": "JSON",
      "schemaDefinition": "ewogICJkZXNjcmlwdGlvbiI6ICJUaGlzIGlzIGEgdGhlIG1vc3QgZ2VuZXJpYyBKU09OIHNjaGVtYSwgc2luY2UgaXQgcmVxdWlyZXMganVzdCBhIEpTT04sIG5vdGhpbmcgZnVydGhlciBzcGVjaWZpZWQiLAogICJ0eXBlIjogIm9iamVjdCIKfQ==",
      "arguments": {}
    }
  ]
}
```

***

## Options

| Option | Long Version | Explanation                                                                       | Required |
|--------|--------------|-----------------------------------------------------------------------------------|:--------:|
| `-i`   | `--id`       | Filter by schemas with a matching schema id. Can be specified multiple times.     |          |
| `-t`   | `--type`     | Filter by schemas of a matching schema type. Can be specified multiple times      |          |
| `-f`   | `--field`    | Select which JSON fields appear in the response. Can be specified multiple times. |          |
|        | `--limit`    | Limit the maximum number of schemas returned.                                     |          |

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
mqtt hivemq schema delete
```

***

## Simple Example

```
mqtt hivemq schema delete --id my-schema-id 
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
