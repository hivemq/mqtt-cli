---
nav_order: 2
redirect_from: /docs/swarm/run.html
---

# Run

***

The run command of the HiveMQ Swarm command line offers a set of commands to start and stop runs.

```
mqtt swarm run
```

***

## Commands

| Command | Explanation             |
|---------|-------------------------|
| start   | See [start](#start-run) |
| stop    | See [stop](#stop-run)   |

***

## Options

| Option | Long Version | Explanation                                | Default                 |
|--------|--------------|--------------------------------------------|-------------------------|
| `-url` |              | The URL of the HiveMQ Swarm Rest endpoint. | `http://localhost:8888` |

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

# Start Run

***

Start a scenario, either blocking or detached.

```
mqtt swarm run
```

***

## Options

| Option | Long Version | Explanation                                           | Default                 |
|--------|--------------|-------------------------------------------------------|-------------------------|
| `-url` |              | The URL of the HiveMQ Swarm Rest endpoint.            | `http://localhost:8888` |
| `-f`   | `--file`     | The scenario to execute.                              | required                |
| `-d`   | `--dettach`  | Execute the scenario in detached (non-blocking) mode. | `The current run`       |

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

## Further Examples

### Blocking Mode

Upload and execute a scenario from a file.
The scenario is deleted on the HiveMQ Swarm Commander afterward.
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

***

### Detached Mode

Upload and execute a scenario from a file.
This command returns immediately after the scenario was started.

```
$ mqtt swarm run start -f /path/to/scenario -d

Successfully uploaded scenario. Scenario-id: 2
Run id: 1
Run status: STARTING
```

***

# Stop Run

Stop a running scenario using the run id.

```
mqtt swarm run stop
```

***

## Options

| Option | Long Version | Explanation                                | Default                 |
|--------|--------------|--------------------------------------------|-------------------------|
| `-url` |              | The URL of the HiveMQ Swarm Rest endpoint. | `http://localhost:8888` |
| `-r`   | `--run-id`   | The id of the run to stop.                 | `The current run`       |

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}
