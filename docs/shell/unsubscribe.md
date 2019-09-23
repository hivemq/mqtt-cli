---
layout: default
title: Unsubscribe
parent: Shell-Mode
nav_order: 5
---

{:.main-header-color-yellow}
# Unsubscribe
***

Unsubscribes the currently active context client from a list of topics
and is therefore only available in Shell mode.

## Synopsis

```
clientID@Host>  unsub   {   -t <topic> [-t <topic>]...
                            [-up <userProperty>]...
}
```

***

## Options


|Option |Long Version | Explanation | Default |
| ------- | -------------- | ------------------------- | -------- |
| ``-t``   | ``--topic``| A topic from which the client will unsubscribe from. |
| ``-up``| ``--userProperty`` | A user property of the unsubscribe message. |

***

## Example

> Connect a client which is identified by myClient and subscribe it to two topics afterwards.
Then unsubscribe from one of the two topics:

```
mqtt> con -i myClient
myClient@localhost> sub -t topic1 -t topic2
myClient@localhost> unsub -t topic1
mqtt>
```