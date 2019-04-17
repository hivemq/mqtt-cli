#!/usr/bin/env bash
java -cp target/mqtt-cli-1.0-SNAPSHOT.jar picocli.codegen.aot.graalvm.ReflectionConfigGenerator com.hivemq.cli.Mqtt > r1.json