package com.hivemq.cli.commands.swarm;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author Yannick Weber
 */
public abstract class AbstractSwarmCommand implements Callable<Integer> {

    @CommandLine.Option(
            names = {"-url"},
            defaultValue = "http://localhost:8888",
            description = "The URL of the HiveMQ Swarm REST API endpoint (default http://localhost:8888)",
            order = 1)
    protected @NotNull String commanderUrl;

    @Override
    public @NotNull String toString() {
        return "AbstractSwarmCommand{" +
                "url='" + commanderUrl + '\'' +
                '}';
    }
}
