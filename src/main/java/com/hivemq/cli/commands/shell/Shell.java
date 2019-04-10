package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.AbstractCommand;
import com.hivemq.cli.commands.Subscribe;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliJLineCompleter;

import java.io.PrintWriter;

@CommandLine.Command(name = "shell",
        description = "Starts the mqtt cli in shell mode, to enable interactive working.",
        footer = {"", "Press Ctl-D to exit."},
        subcommands = {Subscribe.class, ClearScreen.class})
public class Shell extends AbstractCommand implements Runnable {

    LineReaderImpl reader;
    PrintWriter out;

    Shell() {
    }

    public void setReader(LineReader reader) {
        this.reader = (LineReaderImpl) reader;
        out = reader.getTerminal().writer();
    }

    public void run() {
        System.out.println(new CommandLine(this).getUsageMessage());
        interact();
    }

    private void interact() {
        try {
            CommandLine cmd = new CommandLine(this);
            Terminal terminal = TerminalBuilder.builder().build();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(new PicocliJLineCompleter(cmd.getCommandSpec()))
                    .parser(new DefaultParser())
                    .build();
            this.setReader(reader);
            String prompt = "mqtt> ";
            String rightPrompt = "null";

            // start the shell and process input until the user quits with Ctl-D
            String line;
            while (true) {
                try {
                    line = reader.readLine(prompt, rightPrompt, (MaskingCallback) null, null);
                    ParsedLine pl = reader.getParser().parse(line, 5);
                    String[] arguments = pl.words().toArray(new String[0]);
                    CommandLine.run(this, arguments);
                } catch (UserInterruptException e) {
                    // Ignore
                } catch (EndOfFileException e) {
                    return;
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


