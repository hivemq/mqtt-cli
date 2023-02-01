<div align="center">

<a href="https://hivemq.github.io/mqtt-cli/">
<img src="./img/mqtt-logo.svg" width="500"/>
</a>

[![GitHub Release](https://img.shields.io/github/v/release/hivemq/mqtt-cli?style=for-the-badge)](https://github.com/hivemq/mqtt-cli/releases)
[![GitHub Workflow Status (branch)](https://img.shields.io/github/actions/workflow/status/hivemq/mqtt-cli/.github/workflows/check.yml?branch=master&style=for-the-badge)](https://github.com/hivemq/mqtt-cli/actions/workflows/check.yml?query=branch%3Amaster)
[![CLI Downloads](https://img.shields.io/github/downloads/hivemq/mqtt-cli/total?style=for-the-badge)](https://github.com/hivemq/mqtt-cli/releases)
[![CLI License](https://img.shields.io/github/license/hivemq/mqtt-cli?style=for-the-badge)](https://github.com/hivemq/mqtt-cli/blob/develop/LICENSE)

<i>mqtt-cli</i> is a <b>feature-rich MQTT Command Line Interface</b>.

<img align="center" src="./img/pubsub.gif"/>

<br></br>
[Getting Started](#getting-started) •
[Publish](#publish) •
[Subscribe](#subscribe) •
[Shell](#shell) •
[Test](#test) •
[How to Contribute](#how-to-contribute)


</div>

## Getting Started

- [Installation instructions](https://hivemq.github.io/mqtt-cli/docs/installation/) 
- [Building from source](https://hivemq.github.io/mqtt-cli/docs/installation/#building-from-source) 
- [Full documentation](https://hivemq.github.io/mqtt-cli)

## Publish

- Publish a message
- Quick start: `mqtt pub -t your-topic -m "your message" -h your-mqtt-broker.com`
- [Further documentation](https://hivemq.github.io/mqtt-cli/docs/publish/) 

<img align="center" src="./img/publish.gif"/>

## Subscribe

- Subscribe to topics and receive output directly on the console
- Quick start: `mqtt sub -t your-topic -h your-mqtt-broker.com`
- [Further documentation](https://hivemq.github.io/mqtt-cli/docs/subscribe/)

<img align="center" src="./img/subscribe.gif"/>

## Shell

- Enter the <i>mqtt-cli shell</i> mode to access more MQTT functionality
- Quick start: `mqtt sh`
- [Further documentation](https://hivemq.github.io/mqtt-cli/docs/shell/)


<img align="center" src="./img/shell.gif"/>

## Test

- Run tests against a broker to find out its features and limitations
- Quick start: `mqtt test -h your-mqtt-broker.com`
- [Further documentation](https://hivemq.github.io/mqtt-cli/docs/test/)


<img align="center" src="./img/test.gif"/>


## How to Contribute
- If you want to request a feature or report a bug, please [create a GitHub Issue using one of the provided templates](https://github.com/hivemq/mqtt-cli/issues/new/choose)
- If you want to make a contribution to the project, please have a look at the [contribution guide](CONTRIBUTING.md)
