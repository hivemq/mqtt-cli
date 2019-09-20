#!/usr/bin/env bash
java -cp ././../build/libs/hivemq-cli-1.0.0.jar picocli.codegen.aot.graalvm.ReflectionConfigGenerator \
com.hivemq.cli.HiveMQCLIMain > r1.json