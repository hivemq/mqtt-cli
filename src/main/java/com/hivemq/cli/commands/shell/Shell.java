package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.*;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliJLineCompleter;

import java.io.PrintWriter;

@CommandLine.Command(name = "shell",
        description = "Starts the mqtt cli in shell mode, to enable interactive working with further sub commands.",
        footer = {"", "Press Ctl-C to exit."},
        subcommands = {Connect.class, Subscribe.class, Publish.class, Disconnect.class, ClearScreen.class})
public class Shell extends AbstractCommand implements Runnable {

    LineReaderImpl reader;
    PrintWriter out;
    private static final String prompt = "hivemq-cli> ";
    private static final String rightPrompt = "null";

    Shell() {
    }

    public void run() {
        CommandLine cmd = new CommandLine(this);
        System.out.println(cmd.getUsageMessage());
        interact(cmd);
    }

    public void interact(CommandLine cmd) {
        try {

            Terminal terminal = TerminalBuilder.builder().build();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(new PicocliJLineCompleter(cmd.getCommandSpec()))
                    .parser(new DefaultParser())
                    .build();

            String line;
            while (true) {
                try {
                    line = reader.readLine(prompt, null, (MaskingCallback) null, null);
                    ParsedLine pl = reader.getParser().parse(line, prompt.length());
                    String [] arguments = pl.words().toArray(new String[0]);
                    CommandLine.run(this, arguments);
                } catch (UserInterruptException e) {
                    // Ignore
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
        return Shell.class;
    }
}


