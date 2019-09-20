---
layout: default
title: Packages
parent: Installation
nav_order: 1
---

{:.main-header-color-yellow}
# Installation using a package manager
***

> **NOTE**: At least Java 8 is required to run MQTT CLI.

## Homebrew
For **Mac OS X** and **Linux** systems use **[Homebrew](https://brew.sh/)** to install MQTT CLI via the **[MQTT CLI Tap](https://github.com/hivemq/homebrew-mqtt)**.
```
$ brew tap hivemq/mqtt-cli
```
```
$ brew install mqtt-cli
```

***

## Windows Zip

Download the Windows Zipfile from `url` and extract it in your preferred location.
To execute MQTT CLI simply open the Windows Command Prompt with `⊞ Win` + `R` and execute `cmd`.
Navigate into the extracted MQTT CLI folder and execute `mqtt-cli.exe command`.

To quick start the shell simply double-click the `mqtt-cli-shell.exe`.

***

## Debian Package

If you are using a *nix operating system which operates with debian packages you can download the MQTT CLI debian package from `url` via `wget` or `curl` 
and install the package with `sudo dpkg -i`  or `sudo apt install`:


``` 
wget  http:url/mqtt-cli.deb
sudo dpkg -i mqtt-cli.deb
``` 

***

## RPM Package

For Red Hat, Fedora, Mandriva, OpenSuse, CentOS distributions you can use the rpm package provided at `url`.
The preferred way is to install the package via the `yum` package manager. To install the package simply execute:

``` 
yum install -y url.deb
```

