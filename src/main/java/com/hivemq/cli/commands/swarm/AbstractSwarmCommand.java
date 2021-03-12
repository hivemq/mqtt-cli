package com.hivemq.cli.commands.swarm;

import com.google.common.annotations.VisibleForTesting;
import com.hivemq.cli.commands.swarm.run.SwarmRunStartCommand;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author Yannick Weber
 */
public abstract class AbstractSwarmCommand implements Callable<Integer> {

    public enum OutputFormat {
        JSON, PRETTY
    }

    @CommandLine.Option(names = {"-url"}, defaultValue = "http://localhost:8080", description = "The URL of the HiveMQ Swarm REST API endpoint (default http://localhost:8888)", order = 1)
    @VisibleForTesting
    public @NotNull String commanderUrl = "http://localhost:8080";

    @CommandLine.Option(names = {"--format"}, defaultValue = "pretty", description = "The export output format (default pretty)", order = 4)
    protected @NotNull OutputFormat format = OutputFormat.PRETTY;

    @Override
    public @NotNull String toString() {
        return "AbstractSwarmCommand{" +
                "url='" + commanderUrl + '\'' +
                "format='" + format.toString() + '\'' +
                '}';
    }
}
