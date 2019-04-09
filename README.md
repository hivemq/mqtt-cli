# HiveMQ client command line tool **mqtt**

## under Construction! ##

Mqtt is a command line interface for create and connect MQTT5 clients, subscribe to topics and publish messages.

This overview covers command line syntax, describes the command operations, and provides common examples.

##Synopsis:

$ mqtt [flag] [METHOD] URL [ITEM [ITEM]]
See also mqtt --help.

### METHODS
con, sub, pub, auth, disc

### mqtt con
Mqtt works by default with MQTT 5, but a subset of command for MQTT 3  is also available

#### purpose
- creates a client and connect them. The client can stay connected.
- connect Attributes: ClientIdentifier, Will Properties, Will Topic, Will Payload, UserName, Password, 

**Examples:**


`mqtt con : `

- connects an mqtt5 client at localhost with default port 1883

`mqtt -v3 con myHost:`
- connects an mqtt3 client at myhost with default port 1883


`mqtt con :1884 `
- connects an mqtt5 client at localhost with port 1884. 

ClientIdentifier is randomly set, will message is empty, clean session is false


**full syntax**

```
mqtt con : client?
             i=ClientIdentifier, 
             u=UserName, 
             pw={$|'<password>'|@file//<fileName>},
             cs=0
             wp=[<header:value> [<header>:<value>]],
             wt={$|'<Topic>'},
             pp={$|'<payload>'|@file//<fileName>}
```

**alternativ read properties from file**
    
```
mqtt con : client@file//<fileName>
```
- connects an MQTT 5 client and reads the properties from the file.


### mqtt sub
mqtt sub :
mqtt sub myHost:
mqtt sub :1884 

mqtt sub URL cId=xx -a username:pw  - header1:value1, header2:value2 -t  


##Syntax (v2)
Use the following syntax to run hcli commands from your terminal window:

`mqtt [Version] [Broker] [Client] [Command] [Args]`
where MQTT version, broker, client, command and arg are:

**Version**: Specifies the MQTT version. Version is case-insensitive and you can specify either v3 or v5.

**Broker**: Specifies a Broker Uri with protocol, host and port.

**Client**: Specifies an MQTT Client identifier. Client identifier are case-sensitive. 

**Command**: Specifies the operation that you want to perform  with an MQTT Client, for example sub, pub, con, auth.

**Args**: a list of arguments related to the command, arguments that you specify from the command line override default values

---
When performing an operation on multiple broker and with multiple clients, you can specify each by broker and client 
or specify one or more files:

To specify resources by broker, client:

To group clients if they are all the same broker: Broker client1 client2 client{n}.

`Example: mqtt v5 localhost:1883 c1:user/pwd c2.. sub ...`
 
To group clients if they are on different broker: Broker1/client1 Broker2/client2 Broker{n}/client{n}.
 
`Example: mqtt v5 tcp://localhost:1883/c1?user&pwd tcp://localhost:1884/c2 sub ...`

Alternately broker and clients can be specified with the usage of one or more yaml files

`Example: mqtt v3 -f file1 -f file2 -f file{n}`
