/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.cli.ioc;

import com.hivemq.cli.DefaultCLIProperties;
import dagger.Component;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Component(modules = {
        CliModule.class, ShellModule.class, HiveMqModule.class, SwarmModule.class})
public interface MqttCLI {

    @SuppressWarnings("NullableProblems") //Dagger implemented methods do not create @NotNull annotations
    @Named("cli")
    @NotNull CommandLine cli();

    @SuppressWarnings("NullableProblems") //Dagger implemented methods do not create @NotNull annotations
    @Named("shell")
    @NotNull CommandLine shell();

    @SuppressWarnings("NullableProblems") //Dagger implemented methods do not create @NotNull annotations
    @Named("shell-context")
    @NotNull CommandLine shellContext();

    @SuppressWarnings("NullableProblems") //Dagger implemented methods do not create @NotNull annotations
    @NotNull DefaultCLIProperties defaultCLIProperties();
}
