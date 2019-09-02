/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli.ioc;

import com.hivemq.cli.commands.shell.*;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Singleton;

@Module
public class ContextCommandModule {

    @Singleton
    @Provides
    static @NotNull CommandLine provideCommandLine(final @NotNull ShellContextCommand main,
                                                   final @NotNull ContextPublishCommand contextPublishCommand,
                                                   final @NotNull ContextSubscribeCommand contextSubscribeCommand,
                                                   final @NotNull ContextUnsubscribeCommand contextUnsubscribeCommand,
                                                   final @NotNull ContextDisconnectCommand contextDisconnectCommand,
                                                   final @NotNull ContextSwitchCommand contextSwitchCommand,
                                                   final @NotNull ContextExitCommand contextExitCommand,
                                                   final @NotNull ListClientsCommand listClientsCommand,
                                                   final @NotNull ClearScreenCommand clearScreenCommand) {

        return new CommandLine(main)
                .addSubcommand(contextPublishCommand)
                .addSubcommand(contextSubscribeCommand)
                .addSubcommand(contextUnsubscribeCommand)
                .addSubcommand(contextDisconnectCommand)
                .addSubcommand(contextSwitchCommand)
                .addSubcommand(listClientsCommand)
                .addSubcommand(clearScreenCommand)
                .addSubcommand(contextExitCommand);
    }
}
