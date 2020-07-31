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

import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.commandline.CommandErrorMessageHandler;
import com.hivemq.cli.commandline.CommandLineConfig;
import com.hivemq.cli.commands.MqttCLICommand;
import com.hivemq.cli.commands.cli.PublishCommand;
import com.hivemq.cli.commands.cli.SubscribeCommand;
import com.hivemq.cli.commands.cli.TestBrokerCommand;
import com.hivemq.cli.commands.shell.ShellCommand;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

/**
 * @author Georg Held
 */
@Module(includes = HiveMQCLIModule.class)
class CLIModule {

    private static final String PROPERTIES_FILE_PATH =
            System.getProperty("user.home") + File.separator +
                    ".mqtt-cli" + File.separator +
                    "config.properties";

    @Provides
    @Singleton
    @Named("cli")
    static @NotNull CommandLine provideCli(
            final @NotNull MqttCLICommand main,
            final @NotNull PublishCommand publishCommand,
            final @NotNull SubscribeCommand subscribeCommand,
            final @NotNull ShellCommand shellCommand,
            final @NotNull TestBrokerCommand testBrokerCommand,
            final @NotNull @Named("hivemq-cli") CommandLine hivemqCliCommandLine,
            final @NotNull CommandLineConfig config,
            final @NotNull CommandErrorMessageHandler handler) {

        return new CommandLine(main)
                .addSubcommand(publishCommand)
                .addSubcommand(subscribeCommand)
                .addSubcommand(shellCommand)
                .addSubcommand(testBrokerCommand)
                .addSubcommand(hivemqCliCommandLine)
                .setColorScheme(config.getColorScheme())
                .setUsageHelpWidth(config.getCliWidth())
                .setParameterExceptionHandler(handler);
    }

    @Provides
    @Singleton
    static @NotNull DefaultCLIProperties provideDefaultProperties() {
        return new DefaultCLIProperties(PROPERTIES_FILE_PATH);
    }
}
