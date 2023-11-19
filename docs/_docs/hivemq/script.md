---
nav_order: 1
redirect_from: 
    - /docs/hivemq/scripts.html
     -/docs/hivemq/script.html
---

# Scripts

***

The script command of the HiveMQ command line offers a set of commands to work with scripts using a HiveMQ Data Hub API
endpoint.

**NOTE**: The HiveMQ Data Hub is a new product in the HiveMQ platform and commands using it may be subject to breaking
changes in the future.

```
mqtt hivemq script
```

***

## Commands

| Command | Explanation                         |
|---------|-------------------------------------|
| create  | See [Create Script](#create-script) |
| get     | See [Get Script](#get-script)       |
| list    | See [List Scripts](#list-scripts)   |
| delete  | See [Delete Script](#delete-script) |

***

## Options

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Create Script

*** 

Create a new script and upload it to a HiveMQ node.
The script definition may be provided either directly from the command line or from a file.

```
mqtt hivemq script create
```

***

## Simple Example

```
mqtt hivemq script create --id hello_world --description "Yet another hello world script." --file hello.js --type transformation
```

***

## Options

| Option | Long Version      | Explanation                                                                     | Default |                    Required                     |
|--------|-------------------|---------------------------------------------------------------------------------|---------|:-----------------------------------------------:|
| `-i`   | `--id`            | The id of the script to be created.                                             |         |                        X                        |
|        | `--type`          | The function type of the script. (Currently supported types [`TRANSFORMATION`]) |         |                        X                        |
|        | `--definition`    | The source code of the script.                                                  |         | Either `--definition` or `--file`, but not both |
|        | `--description`   | Short human-readable description of what the script does.                       |         |                                                 |
|        | `--file`          | A path to a file containing the source code of the script.                      |         | Either `--definition` or `--file`, but not both |
|        | `--print-version` | Print the assigned script version after creation.                               | `false` |                                                 |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Get Script

*** 

Retrieve a single existing script.

```
mqtt hivemq script get
```

***

## Simple Example

```
$ mqtt hivemq script get --id hello_world 
{
  "id": "hello_world",
  "version": 1,
  "createdAt": "2023-11-03T13:07:15.650Z",
  "description": "This function greets a person.",
  "functionType": "TRANSFORMATION",
  "source": "ZnVuY3Rpb24gdHJhbnNmb3JtKHBlcnNvbikgeyByZXR1cm4gJ2hlbGxvICcgKyBwZXJzb24gfQ=="
}

```

***

## Options

| Option | Long Version | Explanation                                                                       | Required |
|--------|--------------|-----------------------------------------------------------------------------------|:--------:|
| `-i`   | `--id`       | The id of the script to fetch.                                                    |    X     |
| `-f`   | `--field`    | Select which JSON fields appear in the response. Can be specified multiple times. |          |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# List Scripts

*** 

List every script that exists on a HiveMQ node.
This list may optionally be filtered by script id and script type.

```
mqtt hivemq script list
```

***

## Simple Example

```
$ mqtt hivemq script list --id hello_world
{
  "items": [
    {
      "id": "hello_world",
      "version": 1,
      "createdAt": "2023-11-03T13:59:47.262Z",
      "description": "This function greets a person.",
      "functionType": "TRANSFORMATION",
      "source": "ZnVuY3Rpb24gdHJhbnNmb3JtKHBlcnNvbikgeyByZXR1cm4gJ2hlbGxvICcgKyBwZXJzb24gfQ=="
    },
    {
      "id": "hello_world",
      "version": 2,
      "createdAt": "2023-11-03T13:59:48.348Z",
      "description": "This function greets a person.",
      "functionType": "TRANSFORMATION",
      "source": "ZnVuY3Rpb24gdHJhbnNmb3JtKHBlcnNvbikgeyByZXR1cm4gJ2hlbGxvICcgKyBwZXJzb24gfQ=="
    }
  ]
}
```

***

## Options

| Option | Long Version | Explanation                                                                       | Required |
|--------|--------------|-----------------------------------------------------------------------------------|:--------:|
| `-i`   | `--id`       | Filter by scripts with a matching script id. Can be specified multiple times.     |          |
| `-t`   | `--type`     | Filter by scripts of a matching script type. Can be specified multiple times      |          |
| `-f`   | `--field`    | Select which JSON fields appear in the response. Can be specified multiple times. |          |
|        | `--limit`    | Limit the maximum number of scripts returned.                                     |          |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}


***

# Delete Script

*** 

Delete a single existing script.

```
mqtt hivemq script delete
```

***

## Simple Example

```
mqtt hivemq script delete --id hello_world 
```

***

## Options

| Option | Long Version | Explanation                         | Required |
|--------|--------------|-------------------------------------|:--------:|
| `-i`   | `--id`       | The id of the script to be deleted. |    X     |

### API Connection Options

{% include options/api-connection-options.md %}

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}
