---
layout: default
title: Exit
parent: Shell-Mode
nav_order: 9
--- 

{:.main-header-color-yellow}
# Exit
***

Exits the currently active client context.

## Synopsis

```
clientID@host> exit
```
***

## Example

> Connect a client with identifier ``client`` and exit it's context afterwards

```
hivemq-cli> con -i client
client@localhost> exit
hivemq-cli>
```

> **NOTE**: The client is still connected in the shell
 

