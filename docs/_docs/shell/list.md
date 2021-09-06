---
nav_order: 6
redirect_from: /docs/shell/list.html
--- 

# List
***

Lists all the connected clients.

## Synopsis

```
mqtt> ls    [-ahlrstU]
```

***

## Options

|Option |Long Version | Explanation | Default
|---------------|-------------|------------------------------|
| ``-a``    |  | Include disconnected clients. | ``False``
| ``-l``   | ``--long``| Use a long listing format with detailed information about the clients. | ``False``
| ``-r`` | ``--reverse`` | Reverse order while sorting | ``False`` 
| ``-s`` | ``--subscriptions``  | List subscribed topics of the clients. | ``False``
| ``-t`` | ``--time``  | Sort cliens by their creation time. | ``False``
| ``-U`` |   |  Do not sort.  |  ``False``

***

## Examples

> Connect two clients and list them by default settings

```
mqtt> con -i client1
client1@localhost> exit
mqtt> con -i client2
client2@localhost> ls
client1@localhost
client2@localhost
```

***

> Connect a client and show detailed information about it

```
mqtt> con -i client
client@localhost> ls -l
total 1
CONNECTED    11:00:29 client1 localhost  1883 MQTT_5_0 NO_SSL
```

***

> List subscriptions of all connected clients

``` 
client1@localhost> sub -t topic -t topic2 -t topic3
client1@localhost> ls -s
client1@localhost
 -subscribed topics: [topic2, topic3, topic]
```
