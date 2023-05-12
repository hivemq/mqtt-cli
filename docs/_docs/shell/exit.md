---
nav_order: 9
redirect_from: /docs/shell/exit.html
---

# Exit

***

Exits the currently active client context or the shell if used without a context.

```
$ mqtt> exit

Usage: shell exit [-hV]
Exit the shell
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
```

***

## Example

> Connect a client with identifier `client` and exit its context afterward

```
mqtt> con -i client
client@localhost> exit
mqtt>
```

> **NOTE**: The client is still connected in the shell

***

> Exit the Shell

```
mqtt> exit
$> 
```

