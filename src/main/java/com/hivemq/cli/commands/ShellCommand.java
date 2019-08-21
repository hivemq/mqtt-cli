package com.hivemq.cli.commands;

import org.jetbrains.annotations.NotNull;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.SizePolicy;
import org.pmw.tinylog.writers.RollingFileWriter;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliJLineCompleter;

import javax.inject.Inject;
import java.io.PrintWriter;


@CommandLine.Command(name = "shell", aliases = "sh",
        description = "Starts HiveMQ-CLI in shell mode, to enable interactive mode with further sub commands.",
        footer = {"", "Press Ctl-C to exit."},
        mixinStandardHelpOptions = true)
public class ShellCommand extends AbstractCommand implements Runnable {

    static boolean IN_SHELL = false;

    LineReaderImpl reader;

    private static final String prompt = "hmq> ";

    @SuppressWarnings("NullableProblems")
    @CommandLine.Spec
    private @NotNull CommandLine.Model.CommandSpec spec;

    @Inject
    ShellCommand() {
    }

    @CommandLine.Option(names = {"-d", "--debug"}, defaultValue = "true", description = "Enable debug mode.")
    static boolean DEBUG;


    @Override
    public void run() {
        Configurator.defaultConfig()
                .writer(new RollingFileWriter("hmq-mqtt-log.txt", 30, false, new TimestampLabeler("yyyy-MM-dd"), new SizePolicy(1024 * 10)))
                .formatPattern("{date:yyyy-MM-dd HH:mm:ss}: {{level}:|min-size=6} {context:identifier}: {message}")
                .level(Level.INFO)
                .activate();

        if (DEBUG) {
            Configurator.currentConfig()
                    .level(Level.DEBUG)
                    .activate();
            Logger.debug("Command: {}", this);
        }

        final CommandLine cmd = new CommandLine(spec);
        interact(cmd);
    }


    private void interact(final @NotNull CommandLine cmd) {
        try {
            IN_SHELL = true;
            final Terminal terminal = TerminalBuilder.builder().build();
            reader = (LineReaderImpl) LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(new PicocliJLineCompleter(cmd.getCommandSpec()))
                    .parser(new DefaultParser())
                    .build();

            PrintWriter out = terminal.writer();
            out.println(cmd.getUsageMessage());

            String line;
            while (true) {
                try {
                    line = reader.readLine(prompt, null, (MaskingCallback) null, null);
                    final ParsedLine pl = reader.getParser().parse(line, prompt.length());
                    final String[] arguments = pl.words().toArray(new String[0]);
                    cmd.execute(arguments);
                } catch (UserInterruptException e) {
                    if (DEBUG) {
                        Logger.debug("User interrupted shell: {}", e);
                    }
                    return;
                } catch (EndOfFileException e) {
                    // exit shell
                    if (DEBUG) {
                        Logger.error(e);
                    } else {
                        Logger.error(e.getMessage());
                    }
                    // TODO all clients were disconnected
                    return;
                } catch (Exception all) {
                    if (DEBUG) {
                        Logger.error(all);
                    } else {
                        Logger.error(all.getMessage());
                    }
                    System.err.println("Error in command. " + all.getMessage());
                }
            }
        } catch (Throwable t) {
            if (DEBUG) {
                Logger.error(t);
            } else {
                Logger.error(t.getMessage());
            }
        }
    }


    @Override
    public String toString() {
        return "Shell:: {" +
                "debug=" + DEBUG +
                '}';
    }

    @Override
    public Class getType() {
        return ShellCommand.class;
    }
}
