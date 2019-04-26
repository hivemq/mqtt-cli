package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.AbstractCommand;
import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.commands.Subscribe;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import jline.console.completer.ArgumentCompleter.ArgumentList;
import jline.console.completer.ArgumentCompleter.WhitespaceArgumentDelimiter;
import org.jline.reader.EndOfFileException;
import picocli.CommandLine;
import picocli.shell.jline2.PicocliJLineCompleter;

@CommandLine.Command(name = "shell",
        description = "Starts the mqtt cli in shell mode, to enable interactive working.",
        footer = {"", "Press Ctl-C to exit."},
        subcommands = {Connect.class, Subscribe.class, Disconnect.class, ClearScreen.class})
public class Shell extends AbstractCommand implements Runnable {

    ConsoleReader reader;

    Shell() {
    }

    public void run() {
        CommandLine cmd = new CommandLine(this);
        System.out.println(cmd.getUsageMessage());
        interact(cmd);
    }

    public void interact(CommandLine cmd) {
        try {

            reader = new ConsoleReader();
            reader.setPrompt("mqtt> ");

            // set up the completion ;
            reader.addCompleter(new PicocliJLineCompleter(cmd.getCommandSpec()));

            // start the shell and process input until the user quits with Ctl-D
            String line;
            while (true) {
                try {
                    if ((line = reader.readLine()) != null) {
                        ArgumentList list = new WhitespaceArgumentDelimiter().delimit(line, line.length());
                        CommandLine.run(this, list.getArguments());
                    }
                } catch (UserInterruptException e) {
                    // Ignore
                } catch (EndOfFileException e) {
                    return;
                } catch (Exception all) {
                    System.err.println("Error in command.");
                    System.err.println(all.getMessage());
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


