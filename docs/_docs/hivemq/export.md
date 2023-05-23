---
nav_order: 1
redirect_from: /docs/hivemq/export.html
---

# Export

***

The export command of the HiveMQ command line offers a set of commands to export various resources from a HiveMQ API
endpoint.

```
mqtt hivemq export
```

***

## Commands

| Command | Explanation                                         |
|---------|-----------------------------------------------------|
| clients | See [Export Client Details](#export-client-details) |

***

## Options

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Export Client Details

*** 

Export client details from a HiveMQ node via the `export clients` command.

```
mqtt hivemq export clients
```

***

## Simple Example

```
$ mqtt hivemq export clients 

Exporting client details: ...
Successfully exported x client details to hivemq_client_details_2020-07-30-11:06:24.csv
```

**NOTE**: The execution of this command may take a while. Expect an export of 100.000 client details to take at least
several minutes depending on the chosen rate limit.

***

## Options

| Option | Long Version       | Explanation                                                                         | Default                                                                 |
|--------|--------------------|-------------------------------------------------------------------------------------|-------------------------------------------------------------------------|
| `-url` |                    | The URL of the HiveMQ API endpoint.                                                 | `http://localhost:8888`                                                 |
| `-f`   | `--file`           | The file to write the output to.                                                    | If no file is specified a new file is created in the current directory. |
| `-r`   | `--rate`           | The rate limit of the rest calls to the HiveMQ API endpoint in requests per second. | `1500`                                                                  |
|        | `--format`         | The export output format. (Currently supported formats [`csv`])                     | `csv`                                                                   |
|        | `--csvSeparator=`  | The separator for csv export.                                                       | `,`                                                                     |
|        | `--csvQuoteChar`   | The quote character for csv export.                                                 | `"`                                                                     |
|        | `--csvEscChar`     | The escape character for csv export.                                                | `"`                                                                     |
|        | `--csvLineEndChar` | The line-end character for csv export.                                              | `\n`                                                                    |

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}
