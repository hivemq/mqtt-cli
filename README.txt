MQTT CLI
========

MQTT CLI is an MQTT 5.0 and 3.1.1 compatible and feature-rich Command Line Interface


Features
========
- All MQTT 3.1.1 and MQTT 5.0 features are supported
- Interactive, direct and verbose mode for all MQTT Commands
- Shell behavior with syntax highlighting, command completion and history
- Configurable default settings
- Ability to connect various MQTT Clients to different broker simultaneously
- Quick broker tests
- Export information from HiveMQ API endpoints
- Various distributions available


Documentation
=================

For a detailed documentation see the official documentation:
https://hivemq.github.io/mqtt-cli


Prerequisites
-------------

At least Java 8 is required to run MQTT CLI.


Quickstart
----------

Get an overview about the supported commands by typing:
$ mqtt

See also:
$ mqtt --help

Subscribe
---------

Subscribe to a topic on default settings and block the console for output of incoming publishes:
$ mqtt sub -t topic

Subscribe to the topics test1 and test2 on default settings and block the console for incoming publishes:
$ mqtt sub -t test1 - test2

Subscribe to topic test on localhost with port 1884:
$ mqtt sub -t test -h localhost -p 1884

See also:
$ mqtt sub --help


Publish
-------

Publish the message "Hello" to the topic test:
$ mqtt pub -t test -m "Hello"

Publish the message "Hello Tests" to the topics test1 and test2:
$ mqtt pub -t test1 -t test2 -m "Hello Tests"

Publish the message "Hello" on localhost with port 1884:
$ mqtt pub -t test -m "Hello" -h localhost -p 1884

See also:
$ mqtt pub --help


Shell
-----

The shell mode allows connecting various MQTT clients.

$ mqtt shell

The Shell-Mode is based around a client context driven use case.
Therefore, methods like Connect and Disconnect switch the current context of the shell
and commands like Publish and Subscribe always relate to the currently active client context.

Example:

Start the shell:
$ mqtt shell

Connect client identifier:
$ mqtt> con -i myClient

Publish with the new context client:
$ myClient> pub -t test -m msg

Disconnect and remove context:
$ myClient> dis
$ mqtt> ...

In Shell-Mode the following Commands are available without an active context:

Commands:
- con, connect     Connects an MQTT client
- dis, disconnect  Disconnects an MQTT client
- switch           Switch the current context
- ls               List all connected clients of this mqtt-cli session with their respective identifiers
- cls, clear       Clears the screen
- exit

When connected you are switched to the context mode.
In context mode all MQTT commands relate to the currently active client.

The following Commands are available with an active context:

Commands:
-  pub, publish        Publish a message to a list of topics
-  sub, subscribe      Subscribe this MQTT client to a list of topics
-  unsub, unsubscribe  Unsubscribes this MQTT client from a list of topics
-  dis, disconnect     Disconnects this MQTT client
-  switch              Switch the current context
-  ls, list            List all connected clients of this mqtt-cli session with their respective identifiers
-  cls, clear          Clears the screen
-  exit                Exit the current context


Test
----

The test command runs various tests against the specified broker to find out its features and limitations.
By default the test command will use MQTT 3 clients to test the broker first
and will afterwards check the connect restrictions returned by a connect of a MQTT 5 client.
You can alter this behavior by specifying different options when using the command.

Test the public HiveMQ broker:
$ mqtt test -h broker.hivemq.com


HiveMQ
------

The HiveMQ command line argument offers various HiveMQ specific commands.
Show all available commands:

$ mqtt hivemq

The export command of the HiveMQ command line offers a set of commands to export various resources from a HiveMQ API endpoint.
$ mqtt hivemq export

Export client details from a HiveMQ node via the export clients command.
$ mqtt hivemq export clients


Please refer to the detailed MQTT CLI Documentation for more examples and complete command descriptions:
https://hivemq.github.io/mqtt-cli
