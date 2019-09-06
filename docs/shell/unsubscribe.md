---
layout: default
title: Unsubscribe
parent: Shell
nav_order: 3
---

# Unsubscribe

Unsubscribes the currently active context client from a list of topics
and is therefore only available in Shell mode.

#### Synopsis

```
unsub   -t <topic> [-t <topic>]...
        [-u <userProperties>]
```

#### Options:


|Option |Long Version | Explanation | Default |
| ------- | -------------- | ------------------------- | -------- |
| ``-t``   | ``--topic``| A topic from which the client will unsubscribe from. |
| ``-u``| ``--userProperties`` | User properties of the unsubscribe message can be defined like  ``key=value`` for single pair or ``key1=value1\|key2=value2`` for multiple pairs. |
