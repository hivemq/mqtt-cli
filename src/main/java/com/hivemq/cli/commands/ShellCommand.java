package com.hivemq.cli.commands;

import org.jetbrains.annotations.NotNull;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliJLineCompleter;

import javax.inject.Inject;
import java.io.PrintWriter;


@CommandLine.Command(name = "shell",
        description = "Starts HiveMQ-CLI in shell mode, to enable interactive mode with further sub commands.",
        footer = {"", "Press Ctl-C to exit."},
        mixinStandardHelpOptions = true)
public class ShellCommand extends AbstractCommand implements Runnable {

    public static boolean IN_SHELL = false;

    @SuppressWarnings("NullableProblems")
    @CommandLine.Spec
    private @NotNull CommandLine.Model.CommandSpec spec;

    @Inject
    ShellCommand() {
    }

    LineReaderImpl reader;
    private static final String prompt = "hmq> ";


    @Override
    public void run() {
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
                    return;
                } catch (EndOfFileException e) {
                    // exit shell
                    // TODO all clients were disconnected
                    return;
                } catch (Exception all) {
                    System.err.println("Error in command. " + all.getMessage());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    @Override
    public Class getType() {
        return ShellCommand.class;
    }
}
