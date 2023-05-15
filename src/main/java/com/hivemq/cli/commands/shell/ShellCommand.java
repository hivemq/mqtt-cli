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

package com.hivemq.cli.commands.shell;

import com.google.common.base.Throwables;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliJLineCompleter;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "shell",
                     aliases = "sh",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     description = "Starts MqttCLI in shell mode, to enable interactive mode with further sub commands.",
                     footer = {"", "@|bold Press Ctl-C to exit.|@"},
                     synopsisHeading = "%n@|bold Usage|@:  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options|@:%n",
                     commandListHeading = "%n@|bold Commands|@:%n",
                     separator = " ",
                     mixinStandardHelpOptions = true)
public class ShellCommand implements Callable<Integer> {

    private static final @NotNull String DEFAULT_PROMPT = "mqtt> ";
    private static @NotNull String prompt = DEFAULT_PROMPT;

    public static @Nullable PrintWriter TERMINAL_WRITER;

    private static @Nullable LineReaderImpl currentReader;
    private static @Nullable LineReaderImpl shellReader;
    private static @Nullable LineReaderImpl contextReader;
    private static @Nullable CommandLine currentCommandLine;
    private static @Nullable CommandLine shellCommandLine;
    private static @Nullable CommandLine contextCommandLine;
    private static boolean exitShell = false;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-l"},
                        defaultValue = "false",
                        description = "Log to $HOME/.mqtt-cli/logs (Configurable through $HOME/.mqtt-cli/config.properties)")
    private boolean logToLogfile;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Spec
    private @NotNull CommandLine.Model.CommandSpec spec;

    private final @NotNull DefaultCLIProperties defaultCLIProperties;

    private @Nullable String logfilePath;

    @Inject
    ShellCommand(final @NotNull DefaultCLIProperties defaultCLIProperties) {
        this.defaultCLIProperties = defaultCLIProperties;
    }

    @Override
    public @NotNull Integer call() {
        LoggerUtils.setupConsoleLogging(logToLogfile, "warn");
        logfilePath = Configuration.get("writer.file");

        return interact();
    }

    private @NotNull Integer interact() {
        shellCommandLine = Objects.requireNonNull(MqttCLIMain.MQTT_CLI).shell();
        contextCommandLine = MqttCLIMain.MQTT_CLI.shellContext();

        try {
            final Terminal terminal = TerminalBuilder.builder().name("MQTT Terminal").system(true).build();
            shellReader = (LineReaderImpl) LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(new PicocliJLineCompleter(shellCommandLine.getCommandSpec()))
                    .parser(new DefaultParser())
                    .build();

            contextReader = (LineReaderImpl) LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(new PicocliJLineCompleter(contextCommandLine.getCommandSpec()))
                    .parser(new DefaultParser())
                    .build();

            readFromShell();

            TERMINAL_WRITER = terminal.writer();
            TERMINAL_WRITER.println(shellCommandLine.getUsageMessage());
            TERMINAL_WRITER.flush();

            TERMINAL_WRITER.printf("Using default values from properties file %s:\n",
                    Objects.requireNonNull(defaultCLIProperties.getFile()).getPath());
            TERMINAL_WRITER.printf("Host: %s, Port: %d, Mqtt-Version %s, Logfile-Debug-Level: %s\n",
                    defaultCLIProperties.getHost(),
                    defaultCLIProperties.getPort(),
                    defaultCLIProperties.getMqttVersion(),
                    defaultCLIProperties.getLogfileDebugLevel());
            if (logfilePath != null) {
                TERMINAL_WRITER.printf("Writing Logfile to %s\n", logfilePath);
            } else {
                TERMINAL_WRITER.printf("No Logfile used - Activate logging with the 'mqtt sh -l' option\n");
            }

            Logger.info("--- Shell-Mode started ---");

            String line;
            while (!exitShell) {
                try {
                    line = Objects.requireNonNull(currentReader).readLine(prompt, null, (MaskingCallback) null, null);
                    final ParsedLine pl = currentReader.getParser().parse(line, prompt.length());
                    final String[] arguments = pl.words().toArray(new String[0]);
                    if (arguments.length != 0) {
                        Objects.requireNonNull(currentCommandLine).execute(arguments);
                    }
                } catch (final UserInterruptException e) {
                    Logger.trace("--- User interrupted shell ---");
                    return 0;
                } catch (final Exception ex) {
                    Logger.error(ex, Throwables.getRootCause(ex).getMessage());
                    return 1;
                }
            }
            Logger.info("--- Shell-Mode exited ---");
        } catch (final Exception ex) {
            Logger.error(ex, Throwables.getRootCause(ex).getMessage());
            return 1;
        }
        return 0;
    }

    static void exitShell() {
        exitShell = true;
    }

    static void readFromContext() {
        currentReader = contextReader;
        currentCommandLine = contextCommandLine;
        prompt = new AttributedStringBuilder().style(AttributedStyle.BOLD.foreground(AttributedStyle.YELLOW))
                .append(Objects.requireNonNull(ShellContextCommand.contextClient)
                        .getConfig()
                        .getClientIdentifier()
                        .orElse(MqttClientIdentifier.of(""))
                        .toString())
                .style(AttributedStyle.DEFAULT)
                .append("@")
                .style(AttributedStyle.BOLD.foreground(AttributedStyle.YELLOW))
                .append(ShellContextCommand.contextClient.getConfig().getServerHost())
                .style(AttributedStyle.DEFAULT)
                .append("> ")
                .toAnsi();
    }

    static void readFromShell() {
        currentReader = shellReader;
        currentCommandLine = shellCommandLine;
        prompt = new AttributedStringBuilder().style(AttributedStyle.DEFAULT).append(DEFAULT_PROMPT).toAnsi();
    }

    static void usage(final @NotNull Object command) {
        CommandLine.usage(command, System.out);
    }

    static @NotNull String getUsageMessage() {
        return Objects.requireNonNull(currentCommandLine).getUsageMessage();
    }

    static void clearScreen() {
        Objects.requireNonNull(currentReader).clearScreen();
    }

    @Override
    public @NotNull String toString() {
        return "ShellCommand{" +
                "logToLogfile=" +
                logToLogfile +
                ", spec=" +
                spec +
                ", defaultCLIProperties=" +
                defaultCLIProperties +
                ", logfilePath='" +
                logfilePath +
                '\'' +
                '}';
    }
}
