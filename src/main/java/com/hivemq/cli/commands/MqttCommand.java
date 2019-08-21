package com.hivemq.cli.commands;

import com.hivemq.cli.converters.MqttVersionConverter;
import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import picocli.CommandLine;

@CommandLine.Command
public abstract class MqttCommand extends AbstractCommand implements CliCommand {

    private boolean debug;

    @CommandLine.Option(names = {"-v", "--version"}, defaultValue = "5", converter = MqttVersionConverter.class, description = "The mqtt version used by the client.")
    private MqttVersion version;

    @CommandLine.Option(names = {"-d", "--debug"}, defaultValue = "false", description = "Enable debug mode.")
    private void setDebugLevelDebug(boolean b) {
        if (b) {
            Configurator.currentConfig().level(Level.INFO).activate();
        }
    }

    @CommandLine.Option(names = {"-D", "--verbose"}, defaultValue = "false", description = "Enable debug mode.")
    private void setDebugLevelVerbose(boolean b) {
        if (b) {
            this.debug = true;
            Configurator.currentConfig().level(Level.DEBUG).activate();
        }
    }

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "localhost", description = "The host of the message broker..")
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, defaultValue = "1883", description = "The port of the message broker.")
    private int port;

    @CommandLine.Option(names = {"-i", "--identifier"}, description = "The client identifier UTF-8 String.")
    private String identifier;

    public @NotNull MqttVersion getVersion() {
        return version;
    }

    public void setVersion(final @NotNull MqttVersion version) {
        this.version = version;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
