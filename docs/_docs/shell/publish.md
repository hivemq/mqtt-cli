---
nav_order: 3
redirect_from: /docs/shell/publish.html
---

# Publish

***

The publish with a context works almost the same as [Publish](../publish.md), but it will not create a new connection
and publish with a new client.
Instead, it uses the currently active context client.

```
client@host> publish
```

Alias: `client@host> pub`

***

## Options

### Publish Options

{% include options/publish-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=true %}

***

## Example

Publish with a client identified with `myClient` to the default settings:

```
mqtt> con -i myClient
myClient@localhost> pub -t test -m msg
```
