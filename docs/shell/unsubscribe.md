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
                            [-up <userProperties>]
}
```

***

## Options


|Option |Long Version | Explanation | Default |
| ------- | -------------- | ------------------------- | -------- |
| ``-t``   | ``--topic``| A topic from which the client will unsubscribe from. |
| ``-up``| ``--userProperties`` | User properties of the unsubscribe message can be defined like  ``key=value`` for single pair or ``key1=value1|key2=value2`` for multiple pairs. |

***

## Example

> Connect a client which is identified by myClient and subscribe it to two topics afterwards.
Then unsubscribe from one of the two topics:

```
hivemq-cli> con -i myClient
myClient@localhost> sub -t topic1 -t topic2
myClient@localhost> unsub -t topic1
hivemq-cli>
```