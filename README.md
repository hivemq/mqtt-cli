# MQTT Command Line Interface

<img src="01-mqtt-cli.svg" width="500"> 

[![Build Status](https://travis-ci.org/hivemq/01-mqtt-cli.svg?branch=develop)](https://travis-ci.com/hivemq/mqtt-cli) 

MQTT 5.0 and 3.1.1 compatible and feature-rich MQTT Command Line Interface

## Documentation

A detailed documentation can be found [here](https://hivemq.github.io/mqtt-cli)

## Features

- **All MQTT 3.1.1 and MQTT 5.0 features** are supported
- **interactive**, direct and verbose Mode for all MQTT Commands
- Shell behavior with Syntax Highlighting, Command completion and history
- configurable Default settings
- Ability to connect simultaneously various MQTT Clients to different Broker
- Various distributions available
- Graal support coming soon

### Prerequisites
At least Java 8 is required to run MqttCLI.

### Quickstart
The simplest way to start the MQTT CLI is typing:
``` $ mqtt ```
See also ``mqtt --help``.

#### Download latest MQTT CLI package

Packages 
 for **Linux, Mac OS and Windows**
can be found here: 
[Installation/Packages](https://hivemq.github.io/mqtt-cli/docs/installation/packages.html)!

### Building from source
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


#### Basic Examples


##### Subscribe example

|Command                                         |Explanation                                                              |
|------------------------------------------------|-------------------------------------------------------------------------|
| ``mqtt sub -t topic`` | Subscribe to a topic on default settings and block the console.
| ``mqtt sub -t test1 -t test2``| Subscribe to the topics test1 and test2 on default settings and block the console.
| ``mqtt sub -t test -h localhost -p 1884``| Subscribe to topic test at localhost:1884.


See also ``mqtt sub --help``

##### Publish example
|Command                                                |Explanation                                                              |
|-------------------------------------------------------|-------------------------------------------------------------------------|
| ``mqtt pub -t test -m "Hello" `` | Publish the message "Hello" to the test topics with the default settings
| ``mqtt pub -t test1 -t test2 -m "Hello Tests"`` | Publish the message "Hello Tests" on both test topics with the default settings
| ``mqtt pub -t test -m "Hello" -h localhost -p 1884``| Publish the message "Hello" on localhost:1884|

See also ``mqtt pub --help``

#### Shell Mode

* If interacting with several clients, using different contexts and publishing and subscribing with them in various ways, 
the shell mode with further sub commands is useful.

``$>mqtt shell``

The Shell-Mode is based around a client context driven use case.
Therefore methods like Connect and Disconnect switch the current context of the shell and commands like Publish and Subscribe always relate to the currently active client context.

**Example:**

```
mqtt shell                # starts the shell

mqtt> con -i myClient            # connect client with identifier
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
  


Pls. refer to the detailed documentation [MQTT CLI Documentation](https://hivemq.github.io/mqtt-cli)
for more examples and complete command descriptions.


## Versioning

[Semantic Versioning](https://semver.org/) is used.


## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

## License

See [LICENSE](LICENSE)
