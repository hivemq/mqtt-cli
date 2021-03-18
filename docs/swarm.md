---
layout: default
title: HiveMQ Swarm
nav_order: 8
has_children: false
---

{:.main-header-color-yellow}
# HiveMQ Swarm
***

The HiveMQ Swarm command offers various ways to interact with HiveMQ Swarm.


```
$ mqtt swarm
Usage:  mqtt swarm [-hV] [COMMAND]

HiveMQ Swarm Command Line Interpreter.

Options:
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:
  status  Check the status of HiveMQ Swarm.
  run     HiveMQ Swarm Run Command Line Interpreter.
```

***

## Status

The status command of the HiveMQ Swarm command line enables fetching of the HiveMQ Swarm status.

```
$ mqtt swarm status --help 

Usage:  mqtt swarm status [-hV] [--format=<format>] [-url=<commanderUrl>]

Check the status of HiveMQ Swarm. (READY, STARTING, RUNNING, STOPPING).

Options:
      --format=<format>     The export output format (JSON, PRETTY). Default=PRETTY.
  -h, --help                Show this help message and exit.
      -url=<commanderUrl>   The URL of the HiveMQ Swarm REST API endpoint (default http://localhost:8888)
  -V, --version             Print version information and exit.

```
### Synopis

``` 
mqtt swarm status    [-f=<file>]
                     [--format=<format>]
                     [-url=<url>] 
```

### Options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-url``   | | The URL of the HiveMQ Swarm Rest endpoint. | ``http://localhost:8888``
|  | ``--format`` | The export output format. (Currently supported formats [``pretty``]) | ``pretty``

***

## Run

The run command of the HiveMQ Swarm command line offers a set of commands to start and stop runs.

```
$ mqtt swarm run

Usage:  mqtt swarm run [-hV] [--format=<format>] [-url=<commanderUrl>] [COMMAND]

HiveMQ Swarm Run Command Line Interpreter.

Options:
      --format=<format>     The export output format (default pretty)
  -h, --help                Show this help message and exit.
      -url=<commanderUrl>   The URL of the HiveMQ Swarm REST API endpoint (default http://localhost:8888)
  -V, --version             Print version information and exit.

Commands:
  start  Start HiveMQ Swarm runs.
  stop   Stop HiveMQ Swarm runs.

```
### Synopis

``` 
mqtt hivemq run [COMMAND]   
                            [-dhV] 
                            [-f=<scenario>] 
                            [-url=<commanderUrl>]
                            [-r=<run-id>]


Commands:
  start stop
```

### Options

|Option   |Long Version    | Explanation                                         | Default|
|---------|----------------|-----------------------------------------------------|---------|
| ``-url``   | | The URL of the HiveMQ Swarm Rest endpoint. | ``http://localhost:8888``
| ``-f``| ``--file`` | The scenario to execute. | required
| ``-r``| ``--run-id`` | Then id of the run to stop. | ``The current run``
| ``-d``| ``--dettach`` | Execute the scenario in detached mode. | ``The current run``

### Start run directly

Upload and execute a scenario from a file.
The scenario is deleted on the HiveMQ Swarm Commander afterwards.
This command returns after the scenario is finished.
Stopping the process will also stop the scenario.

```
$ mqtt swarm run start -f /path/to/scenario

Successfully uploaded scenario. Scenario-id: 2
Run id: 1
Run status: STARTING
Run status: RUNNING
Scenario Stage: Stage with id 's1' (1/3).
Run status: RUNNING
Scenario Stage: Stage with id 's2' (2/3).
Run status: RUNNING
Scenario Stage: Stage with id 's3' (3/3).
Run status: FINISHED
Scenario Stage: Stage with id 's3' (3/3).
```

### Start run directly (detached)

Upload and execute a scenario from a file.
This command returns immediately after the scenario was started.

```
$ mqtt swarm run start -f /path/to/scenario -d

Successfully uploaded scenario. Scenario-id: 2
Run id: 1
Run status: STARTING
```

### Stop run

```
$ mqtt swarm run stop -r 1
```
