---
nav_order: 1
redirect_from: /docs/shell/connect.html
---

# Connect

***

The connect command creates a client and connects it to the specified broker.
The client will stay connected until it is disconnected by the broker or the [disconnect](disconnect.md) method is
called.
To list all the connected clients of this mqtt-cli shell session use the [list](list.md) method.

```
mqtt> connect
```

Alias: `mqtt> con`

## Simple Examples

| Command                            | Explanation                                                                                           |
|------------------------------------|:------------------------------------------------------------------------------------------------------|
| `mqtt> con`                        | Creates and connects a new MQTT client with the default settings.                                     |
| `mqtt> con -V 3 -h myHost`         | Creates and connects an MQTT 3.1.1 client at myHost with the default port.                            |
| `mqtt> con -i mqtt-client -p 1884` | Creates and connects an MQTT client at localhost with port 1884 which is identified by `mqtt-client`. |

***

## Options

### Connect Options

{% include options/connect-options.md %}

#### Will Options

{% include options/will-options.md %}

#### Connect Restrictions

{% include options/connect-restrictions-options.md %}

### Security Options

#### Credentials Authentication

{% include options/authentication-options.md %}

#### TLS Authentication

{% include options/tls-options.md %}

### Help Options

{% include options/help-options.md defaultHelp=false %}

***

## Examples

Connect a client to myHost on port 1884

```
mqtt> con -h myHost -p 1884
```

***

Connect a client to the default host and port using authentication

```
mqtt> con -u username -pw password
# Or omit the password to get it prompted
mqtt> con -u username -P
Enter value for --password (The password for the client UTF-8 String.):
```

***

Connect a client with default settings and use it to publish

```
mqtt> con -i myClient
myClient@localhost> pub -t test -m "Hello World"
```

***

Connect a client with a will message

```
mqtt> con -wt willtopic -wq 2 -wm "Client disconnected ungracefully"
```

***

Connect a client with SSL using client side and server side authentication with a password encrypted private key

```
mqtt> con --cafile pathToServerCertificate.pem --tls-version TLSv.1.3
         --cert pathToClientCertificate.pem --key pathToClientKey.pem
Enter private key password:
```

***

Connect a client which is identified by myClient and disconnect it afterward using default settings

```
mqtt> con -i myClient
myClient@localhost> dis
mqtt>
```

***

Connect a client which is identified by myClient on specific settings and disconnect it afterward

```
mqtt> con -i myClient -h broker.hivemq.com -V 3
myClient@localhost> exit  # client is still connected
mqtt> dis -i myClient -h broker.hivemq.com
```

**NOTE**: When specifying the **identifier** in order to uniquely identify the desired client, the **hostname** must
also be provided.
If you don't specify these, the default settings for these attributes will be used which may lead to unexpected
behavior.
