---
nav_order: 2
redirect_from:
    - /docs/installation/packages.html
    - /docs/installation.html
---

# Installation

***

## Prerequisites

At least Java 8 is required to run MQTT CLI.

The CLI is implemented with [Java 11](https://www.azul.com/downloads/?version=java-11-lts&package=jdk#zulu) which is the
preferred version to run this project.

***

## Docker

You can run MQTT-CLI on every OS which supports Docker.
To execute a simple command use the following syntax:

```
docker run hivemq/mqtt-cli <your-command>
```

To start the Shell-Mode of the CLI you need to add the `-it` flags to the docker-command:

```
docker run -it hivemq/mqtt-cli shell
```

***

## Homebrew

For **Mac OS X** and **Linux** systems use **[Homebrew](https://brew.sh/)** to install MQTT CLI via the
**[MQTT CLI Tap](https://github.com/hivemq/homebrew-mqtt-cli)**.

```
brew install hivemq/mqtt-cli/mqtt-cli
```

**NOTE**: If you encounter an error like `Java 1.8+ is required to install this formula` please install a java version
higher than 1.8. You can use `brew install --cask zulu` to install the latest release of Azul Zulu OpenJDK.

**NOTE**: As latency-issues may slow down the CLI under **Mac OS X** please verify that you have the
entry `127.0.0.1 localhost your-pc-name` specified under `/etc/hosts`.
You can use `sudo sh -c "echo 127.0.0.1 localhost $(hostname) >> /etc/hosts"` to append this configuration to your
hosts file.

***

## Windows Zip

Download the [Windows Zipfile](https://github.com/hivemq/mqtt-cli/releases/download/v4.20.0/mqtt-cli-4.20.0-win.zip) and
extract it in your preferred location.
To execute MQTT CLI simply open the Windows Command Prompt with `⊞ Win` + `R` and execute `cmd`.
Navigate into the extracted MQTT CLI folder and execute `mqtt-cli.exe`.

To quick start the shell simply double-click the `mqtt-cli-shell.cmd` file.

***

## Debian Package

If you are using a *nix operating system which operates with debian packages you can download the MQTT CLI debian
package from the [releases page](https://github.com/hivemq/mqtt-cli/releases) via `wget` or `curl`
and install the package with `sudo dpkg -i`  or `sudo apt install`:

``` 
wget https://github.com/hivemq/mqtt-cli/releases/download/v4.20.0/mqtt-cli-4.20.0.deb
sudo apt install ./mqtt-cli-4.20.0.deb
``` 

***

## RPM Package

For Red Hat, Fedora, Mandriva, OpenSuse, CentOS distributions you can use the rpm package provided
at [releases page](https://github.com/hivemq/mqtt-cli/releases).
The preferred way is to install the package via the `yum` package manager. To install the package simply execute:

``` 
sudo yum install -y https://github.com/hivemq/mqtt-cli/releases/download/v4.20.0/mqtt-cli-4.20.0.rpm
```

***

## Building From Source

- mqtt-cli uses [Gradle](https://gradle.org/) to build.
- To be able to execute integration tests a running **Docker environment** is required
- To be able to build and test the native image a **GraalVM installation** is required. You can set it up
  with `./gradlew installNativeImageTooling`.

To do a clean build, issue the following command:
`./gradlew clean build`

This runs the unit tests and compiles a new mqtt-cli-<version>.jar into build/libs.
You can then update an existing MQTT CLI installation by replacing its mqtt-cli-<version>.jar with this one.

The `build.gradle.kts` file contains further instructions for building the platform specific distribution packages.
In a nutshell:

For MacOS/Linux brew:
`./gradlew buildBrewFormula`

For the Debian package:
`./gradlew buildDebianPackage`

For the RPM package:
`./gradlew buildRpmPackage`

For the Windows installer:
`./gradlew buildWindowsZip`

For building a local docker image:
`./gradlew jibDockerBuild`
