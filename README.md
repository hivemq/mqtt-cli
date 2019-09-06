# HiveMQ Command Line Interface

<img src="01-hivemq-cli.svg" width="500"> 

[![Build Status](https://travis-ci.org/hivemq/01-hivemq-cli.svg?branch=develop)](https://travis-ci.org/hivemq/hivemq-cli) 

MQTT 5.0 and 3.1.1 compatible and feature-rich MQTT Command Line Interface

## Documentation

A detailed documentation can be found [here](https://hivemq.github.io/hivemq-cli)

## Features

- **All MQTT 3.1.1 and MQTT 5.0 features** are supported
- **interactive**, direct and verbose Mode for all MQTT Commands
- Shell behavior with Syntax Highlighting, Command history
- configurable Default settings
- Ability to connect simultaneously various MQTT Clients to different Broker
- Various distributions available
- Graal support

### Prerequisites
At least Java 8 is required to run HiveMQ-CLI.

### Quickstart
The simplest way to start the CLI is typing:
``` $ hivemq-cli ```
See also ``hivemq-cli --help``.

#### Download latest HiveMQ-CLI package
##### for linux
##### for mac
##### for windows


### Building from source

### Snapshots

Snapshots can be obtained using [JitPack](https://jitpack.io/#hivemq/hivemq-cli).

JitPack works for all branches and also specific commits by specifying in the version.


#### Basic Examples


##### Subscribe example

|Command                                         |Explanation                                                              |
|------------------------------------------------|-------------------------------------------------------------------------|
| ``hivemq-cli sub -t topic`` | Subscribe to a topic on default settings and block the console.
| ``hivemq-cli sub -t test1 -t test2``| Subscribe to the topics test1 and test2 on default settings and block the console.
| ``hivemq-cli sub -t test -h localhost -p 1884``| Subscribe to topic test at localhost:1884.


See also ``hivemq-cli sub --help``

##### Publish example
|Command                                                |Explanation                                                              |
|-------------------------------------------------------|-------------------------------------------------------------------------|
| ``hivemq-cli pub -t test -m "Hello" `` | Publish the message "Hello" to the test topics with the default settings
| ``hivemq-cli pub -t test1 -t test2 -m "Hello Tests"`` | Publish the message "Hello Tests" on both test topics with the default settings
| ``hivemq-cli pub -t test -m "Hello" -h localhost -p 1884``| Publish the message "Hello" on localhost:1884|

See also ``hivemq-cli pub --help``

#### Shell Mode

* If interacting with several clients, using different contexts and publishing and subscribing with them in various ways, 
the shell mode with further sub commands is useful.

``$>hivemq-cli shell``

The Shell-Mode is based around a client context driven use case.
Therefore methods like Connect and Disconnect switch the current context of the shell and commands like Publish and Subscribe always relate to the currently active client context.

**Example:**

```
hivemq-cli shell                # starts the shell

hmq> con -i myClient            # connect client with identifier
myClient> pub -t test -m msg    # publish with new context client
myClient> dis                   # disconnect and remove context
hmq> ...
```
In Shell-Mode the following Commands are available **without** an active context:

* [Connect (in shell)]
* [Disconnect]
* [Switch]
* [List]
* [Clear]

In Shell-Mode the following Commands are available **with** an active context:

* [Publish (with context)]
* [Subscribe (with context)]
* [Unsubscribe]
* [Disconnect]
* [Switch]
* [Exit]
* [List]
* [Clear]


Pls. refer to the detailed documentation [HiveMQ-CLI Documentation](https://hivemq.github.io/hivemq-cli)
for more examples and complete command descriptions.


## Versioning

[Semantic Versioning](https://semver.org/) is used.


## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

## License

See [LICENSE](LICENSE)
