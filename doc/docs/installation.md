---
layout: default
title: Installation
nav_order: 2
has_children: true
---

{:.main-header-color-yellow}
# Installation
***

## Building from source

The MQTT CLI project uses [Gradle](https://gradle.org/) to build. A gradle wrapper configuration is included, so that after cloning the 
[repository](https://github.com/hivemq/mqtt-cli) from GitHub, you can simply change into the directory containing the project and execute 

```
./gradlew build
```

## Prerequisites

At least Java 8 is required to run MQTT CLI.

The CLI is implemented with the [11.0.4 Java release](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot) which is the preferred version to run this project.


