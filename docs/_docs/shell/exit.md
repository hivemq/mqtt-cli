---
nav_order: 9
redirect_from: /docs/shell/exit.html
---

# Exit

***

Exits the currently active client context or the shell if used without a context.

```
mqtt> exit
```

***

## Options

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

## Example

Connect a client with identifier `client` and exit its context afterward

```
mqtt> con -i client
client@localhost> exit
mqtt>
```

**NOTE**: The client is still connected in the shell

***

Exit the Shell

```
mqtt> exit
$
```

