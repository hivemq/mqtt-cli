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

package com.hivemq.cli.commands.shell;

import com.hivemq.cli.HiveMQCLIMain;
import com.hivemq.cli.ioc.DaggerContextCommandLine;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.SizePolicy;
import org.pmw.tinylog.writers.ConsoleWriter;
import org.pmw.tinylog.writers.RollingFileWriter;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliJLineCompleter;

import javax.inject.Inject;
import java.io.PrintWriter;


@CommandLine.Command(name = "shell", aliases = "sh",
        description = "Starts HiveMQ-CLI in shell mode, to enable interactive mode with further sub commands.",
        footer = {"", "@|bold Press Ctl-C to exit.|@"},
        synopsisHeading = "%n@|bold Usage|@:  ",
        descriptionHeading = "%n",
        optionListHeading = "%n@|bold Options|@:%n",
        commandListHeading = "%n@|bold Commands|@:%n",
        separator = " ",
        mixinStandardHelpOptions = true)

public class ShellCommand implements Runnable {

    private static final String DEFAULT_PROMPT = "hivemq-cli> ";
    private static String prompt = DEFAULT_PROMPT;

    public static final boolean DEBUG = true;
    public static final boolean VERBOSE = true;
    private String logfilePath;

    private static LineReaderImpl currentReader;
    private static LineReaderImpl shellReader;
    private static LineReaderImpl contextReader;

    private static CommandLine currentCommandLine;
    private static CommandLine shellCommandLine;
    private static CommandLine contextCommandLine;

    private static boolean exitShell = false;

    private static boolean verbose = false;
    private static boolean debug = false;

    @SuppressWarnings("NullableProblems")
    @CommandLine.Spec
    private @NotNull CommandLine.Model.CommandSpec spec;

    @Inject
    ShellCommand() {
    }


    @Override
    public void run() {

        final String logfileFormatPattern = "{date:yyyy-MM-dd HH:mm:ss}: {{level}:|min-size=6} Client {context:identifier}: {message}";

        final String tmpDir = System.getProperty("java.io.tmpdir");

        final RollingFileWriter logfileWriter = new RollingFileWriter(tmpDir + "/hmq-mqtt-log.txt", 30, false, new TimestampLabeler("yyyy-MM-dd"), new SizePolicy(1024 * 10));

        // TODO Read default config for debug and verbose from a property file
        Configurator.defaultConfig()
                .writer(logfileWriter,
                        Level.TRACE,
                        logfileFormatPattern)
                .addWriter(new ConsoleWriter(),
                        Level.INFO,
                        "{message}")
                .activate();

        logfilePath = logfileWriter.getFilename();

        if (VERBOSE) {
            Logger.trace("Command: {} ", this);
        }


        interact();
    }


    private void interact() {
        shellCommandLine = new CommandLine(spec);
        contextCommandLine = DaggerContextCommandLine.create().contextCommandLine();

        shellCommandLine.setColorScheme(HiveMQCLIMain.COLOR_SCHEME);
        contextCommandLine.setColorScheme(HiveMQCLIMain.COLOR_SCHEME);
        contextCommandLine.setUsageHelpWidth(HiveMQCLIMain.CLI_WIDTH);

        try {
            final Terminal terminal = TerminalBuilder
                    .builder()
                    .build();

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


            final PrintWriter terminalWriter = terminal.writer();
            terminalWriter.println(shellCommandLine.getUsageMessage());
            terminalWriter.flush();

            Logger.info("Writing Logfile to {}", logfilePath);

            String line;
            while (!exitShell) {
                try {
                    line = currentReader.readLine(prompt, null, (MaskingCallback) null, null);
                    final ParsedLine pl = currentReader.getParser().parse(line, prompt.length());
                    final String[] arguments = pl.words().toArray(new String[0]);
                    currentCommandLine.execute(arguments);
                } catch (final UserInterruptException e) {
                    if (VERBOSE) {
                        Logger.trace("User interrupted shell: {}", e);
                    }
                    return;
                } catch (final EndOfFileException e) {
                    if (VERBOSE) {
                        Logger.trace(e);
                    }
                    Logger.error(e.getMessage());
                    return;
                } catch (final Exception all) {
                    if (VERBOSE) {
                        Logger.error(all);
                    }
                    Logger.error(all.getMessage());
                }
            }
        } catch (final Throwable t) {
            if (VERBOSE) {
                Logger.trace(t);
            }
            Logger.error(t.getMessage());

        }
    }

    static void exitShell() {
        exitShell = true;
    }

    static void readFromContext() {
        currentReader = contextReader;
        currentCommandLine = contextCommandLine;
        prompt = new AttributedStringBuilder()
                .style(AttributedStyle.BOLD.foreground(AttributedStyle.YELLOW))
                .append(ShellContextCommand.contextClient.getConfig().getClientIdentifier().get().toString())
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
        prompt = new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT)
                .append(DEFAULT_PROMPT)
                .toAnsi();
    }

    static void usage(Object command) {
        currentCommandLine.usage(command, System.out, HiveMQCLIMain.COLOR_SCHEME);
    }

    static String getUsageMessage() {
        return currentCommandLine.getUsageMessage();
    }

    static void clearScreen() {
        currentReader.clearScreen();
    }


    static boolean isVerbose() {
        return VERBOSE;
    }

    static boolean isDebug() {
        return DEBUG;
    }


    @Override
    public String toString() {
        return "Shell:: {" +
                "logfilePath=" + logfilePath +
                ", debug=" + DEBUG +
                ", verbose=" + VERBOSE +
                "}";
    }


}
