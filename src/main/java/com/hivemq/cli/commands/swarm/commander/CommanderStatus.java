package com.hivemq.cli.commands.swarm.commander;

import org.jetbrains.annotations.NotNull;

/**
 * @author Yannick Weber
 */
public class CommanderStatus {

    private final @NotNull String status;

    public CommanderStatus(final @NotNull String status) {
        this.status = status;
    }

    public @NotNull String getStatus() {
        return status;
    }
}
