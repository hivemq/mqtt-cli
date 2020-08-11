<p align="center">
  <a href="https://hivemq.github.io/mqtt-cli/">
    <img src="/img/mqtt-logo.svg" width="500"/>
  </a>
</p>

# MQTT Command Line Interface

[![GitHub Release](https://img.shields.io/github/v/release/hivemq/mqtt-cli)](https://github.com/hivemq/mqtt-cli/releases) 
[![Build Status](https://travis-ci.com/hivemq/mqtt-cli.svg?branch=develop)](https://travis-ci.com/hivemq/mqtt-cli) 
[![CLI Downloads](https://img.shields.io/github/downloads/hivemq/mqtt-cli/total)](https://github.com/hivemq/mqtt-cli/releases)
[![CLI License](https://img.shields.io/github/license/hivemq/mqtt-cli)](https://github.com/hivemq/mqtt-cli/blob/develop/LICENSE)
[![MQTT Client](https://img.shields.io/badge/hivemq--mqtt--client-1.2.0-green)](https://github.com/hivemq/hivemq-mqtt-client)
[![HiveMQ Testcontainer](https://img.shields.io/badge/testcontainer-1.1.1-green.svg)](https://github.com/hivemq/hivemq-testcontainer)
[![picocli](https://img.shields.io/badge/picocli-4.5.0-green.svg)](https://github.com/remkop/picocli)

MQTT 5.0 and 3.1.1 compatible and feature-rich MQTT Command Line Interface

<p align="center"><img src="/img/shell-mode.gif"/></p>

## Documentation

A detailed documentation can be found [here](https://hivemq.github.io/mqtt-cli)

## Features

- **All MQTT 3.1.1 and MQTT 5.0 features** are supported
- **interactive**, direct and verbose Mode for all MQTT Commands
- Shell behavior with Syntax Highlighting, Command completion and history
- Configurable default settings
- Ability to connect various MQTT Clients to different broker simultaneously
- Quick broker tests
- Export information from HiveMQ API endpoints
- Various distributions available

## Prerequisites
At least Java 8 is required to run MqttCLI.

## Quickstart
The simplest way to start the MQTT CLI is typing:

``` $ mqtt ```

See also:
 
 ``mqtt --help``.

### Download latest MQTT CLI package

Packages 
 for **Linux, Mac OS and Windows**
can be found here: 
[Installation/Packages](https://hivemq.github.io/mqtt-cli/docs/installation/packages.html)!

## Building from source
To do a clean build, issue the following command:

`$ ./gradlew clean build
`
This runs the unit tests and compiles a new mqtt-cli-<version>.jar into build/libs. 
You can then update an existing MQTT CLI installation by replacing its mqtt-cli-<version>.jar with this one.

The `build.gradle` file contains further instructions for building the platform specific distribution packages. 
In a nutshell:


For MacOS/Linux brew:
`$ ./gradlew buildBrewFormula
`

For the Debian package:
`$ ./gradlew buildDebianPackage 
`

For the RPM package:
`$ ./gradlew buildRpmPackage 
`

For the Windows installer:
`$ ./gradlew buildWindowsZip
`

## Subscribe

|Command                                         |Explanation                                                              |
|------------------------------------------------|-------------------------------------------------------------------------|
| ``mqtt sub -t topic`` | Subscribe to a topic on default settings and block the console.
| ``mqtt sub -t test1 -t test2``| Subscribe to the topics test1 and test2 on default settings and block the console.
| ``mqtt sub -t test -h localhost -p 1884``| Subscribe to topic test at localhost:1884.


See also ``mqtt sub --help``

## Publish
|Command                                                |Explanation                                                              |
|-------------------------------------------------------|-------------------------------------------------------------------------|
| ``mqtt pub -t test -m "Hello" `` | Publish the message "Hello" to the test topics with the default settings
| ``mqtt pub -t test1 -t test2 -m "Hello Tests"`` | Publish the message "Hello Tests" on both test topics with the default settings
| ``mqtt pub -t test -m "Hello" -h localhost -p 1884``| Publish the message "Hello" on localhost:1884|

See also ``mqtt pub --help``

## Shell

* If interacting with several clients, using different contexts and publishing and subscribing with them in various ways, 
the shell mode with further sub commands is useful.

``$ mqtt shell``

The Shell-Mode is based around a client context driven use case.
Therefore methods like Connect and Disconnect switch the current context of the shell and commands like Publish and Subscribe always relate to the currently active client context.

```
mqtt shell                # starts the shell

mqtt> con -i myClient           # connect client with identifier
myClient> pub -t test -m msg    # publish with new context client
myClient> dis                   # disconnect and remove context
mqtt> ...
```
In Shell-Mode the following Commands are available **without** an active context:

**Commands** 
* con, connect     Connects an mqtt client
* dis, disconnect  Disconnects an mqtt client
* switch           Switch the current context
* ls               List all connected clients with their respective identifieres
* cls, clear       Clears the screen
* exit  

When connected you are switched to the context mode.
In context mode all mqtt commands relate to the currently active client.

The following Commands are available **with** an active context:

**Commands:**
*  pub, publish        Publish a message to a list of topics
*  sub, subscribe      Subscribe this mqtt client to a list of topics
*  unsub, unsubscribe  Unsubscribes this mqtt client from a list of topics
*  dis, disconnect     Disconnects this mqtt client
*  switch              Switch the current context
*  ls, list            List all connected clients with their respective identifieres
*  cls, clear          Clears the screen
*  exit                Exit the current context

  
## Test

The test command runs various tests against the specified broker to find out its features and limitations.
By default the test command will use MQTT 3 clients to test the broker first
and will afterwards check the connect restrictions returned by a connect of a MQTT 5 client.
You can alter this behavior by specifying different options when using the command.

Test the public HiveMQ broker:
``` 
$ mqtt test -h broker.hivemq.com
MQTT 3: OK
        - Maximum topic length: 65535 bytes
        - QoS 0: Received 10/10 publishes in 25,74ms
        - QoS 1: Received 10/10 publishes in 26,27ms
        - QoS 2: Received 10/10 publishes in 70,01ms
        - Retain: OK
        - Wildcard subscriptions: OK
        - Shared subscriptions: OK
        - Payload size: >= 100000 bytes
        - Maximum client id length: 65535 bytes
        - Unsupported Ascii Chars: ALL SUPPORTED
MQTT 5: OK
        - Connect restrictions: 
                > Retain: OK
                > Wildcard subscriptions: OK
                > Shared subscriptions: OK
                > Subscription identifiers: OK
                > Maximum QoS: 2
                > Receive maximum: 10
                > Maximum packet size: 268435460 bytes
                > Topic alias maximum: 5
                > Session expiry interval: Client-based
                > Server keep alive: Client-based

```

## HiveMQ

The HiveMQ command line argument offers various HiveMQ specific commands.

Show all available commands:

``$ mqtt hivemq``

The export command of the HiveMQ command line offers a set of commands to export various resources from a HiveMQ API endpoint.

``$ mqtt hivemq export``

Export client details from a HiveMQ node via the export clients command.

``$ mqtt hivemq export clients``



Please refer to the detailed documentation [MQTT CLI Documentation](https://hivemq.github.io/mqtt-cli)
for more examples and complete command descriptions.


## Versioning

[Semantic Versioning](https://semver.org/) is used.


## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

## License

See [LICENSE](LICENSE)
