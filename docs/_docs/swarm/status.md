---
nav_order: 1
redirect_from: /docs/swarm/status.html
---

# Status

***

The status command of the HiveMQ Swarm command line enables fetching of the HiveMQ Swarm status.

```
mqtt swarm status
```

***

## Options

| Option | Long Version | Explanation                                                        | Default                 |
|--------|--------------|--------------------------------------------------------------------|-------------------------|
| `-url` |              | The URL of the HiveMQ Swarm Rest endpoint.                         | `http://localhost:8888` |
|        | `--format`   | The export output format (Currently supported formats [`pretty`]). | `pretty`                |

### Logging Options

{% include options/logging-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}
