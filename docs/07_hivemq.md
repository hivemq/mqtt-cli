---
layout: default
title: HiveMQ
nav_order: 7
has_children: true
---

{:.main-header-color-yellow}
# HiveMQ
***

The HiveMQ command line argument offers various HiveMQ specific commands. 


```
$ mqtt hivemq

Usage:  hivemq [-hV] [COMMAND]

HiveMQ Command Line Interpreter.

Options:
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:
  export  Exports the specified details from a HiveMQ API endpoint

```

***

## Export

The export command of the HiveMQ command line offers a set of commands to export various resources from a HiveMQ API endpoint.

```
$ mqtt hivemq export

Usage:  hivemq export [-hV] [COMMAND]

Exports the specified details from a HiveMQ API endpoint

Options:
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:
  clients  Export HiveMQ client details
```

### Options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-url``   | | The URL of the HiveMQ API endpoint. | ``http://localhost:8888``
| ``-f``| ``--file`` | The file to write the output to. | If no file is specified a new file is created in the current directory.
| ``-r``| ``--rate`` | The rate limit of the rest calls to the HiveMQ API endpoint in requests per second. | ``1500`` 
|  | ``--format`` | The export output format. (Currently supported formats [``csv``]) | ``csv``
| | `` --csvLineSeparator`` | An own defined line separator for the csv writing. | ``,``
| | `` --csvQuoteChar`` | An own defined quote character for the csv writing. | ``"``
| | `` --csvEscChar`` | An own defined escape character for the csv writing. | ``"``
| | `` --csvLineEndChar`` | An own defined line end character for the csv writing. | ``\n``

### Export client details

Export client details from a HiveMQ node via the `export clients` command. 

```
$ mqtt hivemq export clients 

Exporting client details: ...
Successfully exported x client details to hivemq_client_details_2020-07-30-11:06:24.csv
```

> **NOTE**: The execution of this command may take a while. Expect an export of 100.000 client details to take at least several minutes depending on the chosen rate limit.

