package com.hivemq.cli;

import com.hivemq.cli.ioc.DaggerHiveMQCLI;
import com.hivemq.cli.mqtt.ClientCache;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.PropertiesUtils;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.ConsoleWriter;
import picocli.CommandLine;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class HiveMQCLIMain {


    public final static CommandLine.Help.ColorScheme COLOR_SCHEME = new CommandLine.Help.ColorScheme.Builder(CommandLine.Help.Ansi.ON)
            .commands(CommandLine.Help.Ansi.Style.bold, CommandLine.Help.Ansi.Style.fg_yellow)
            .options(CommandLine.Help.Ansi.Style.italic, CommandLine.Help.Ansi.Style.fg_yellow)
            .parameters(CommandLine.Help.Ansi.Style.fg_yellow)
            .optionParams(CommandLine.Help.Ansi.Style.italic)
            .build();

    public static final int CLI_WIDTH = 160;



    public static void main(final String[] args) {


        Security.setProperty("crypto.policy", "unlimited");

        final CommandLine commandLine = setupCommandLine();

        if (args.length == 0) {
            System.out.println(commandLine.getUsageMessage());
            System.exit(0);
        }

        try {
            setupProperties();
        }
        catch (final Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new DisconnectAllClientsTask());

        final int exitCode = commandLine.execute(args);

        System.exit(exitCode);

    }

    private static void setupProperties() throws Exception {
        Properties properties = null;

        properties = PropertiesUtils.readDefaultProperties(PropertiesUtils.PROPERTIES_FILE_PATH);

        if (properties == null) {
            PropertiesUtils.createDefaultPropertiesFile(PropertiesUtils.DEFAULT_PROPERTIES, PropertiesUtils.PROPERTIES_FILE_PATH);
        }
        else {
            PropertiesUtils.setDefaultProperties(properties);
        }
    }



    private static CommandLine setupCommandLine() {
        final CommandLine commandLine = DaggerHiveMQCLI.create().commandLine();

        commandLine.setColorScheme(COLOR_SCHEME);

        Configurator.defaultConfig()
                .writer(new ConsoleWriter())
                .formatPattern("Client {context:identifier}: {message}")
                .level(Level.INFO)
                .activate();
        commandLine.setUsageHelpWidth(CLI_WIDTH);

        return commandLine;
    }

    private static class DisconnectAllClientsTask extends Thread {

        @Override
        public void run() {
            final ClientCache<String, MqttClient> cache = MqttClientExecutor.getClientCache();
            cache.setVerbose(false);
            final Set<String> keys = cache.keySet();

            final List<CompletableFuture<Void>> disconnectFutures = new ArrayList<CompletableFuture<Void>>();

            for (final String key : keys) {
                final MqttClient client = cache.get(key);
                if (client.getConfig().getState().isConnectedOrReconnect()) {
                    switch (client.getConfig().getMqttVersion()) {
                        case MQTT_5_0:
                            disconnectFutures.add(((Mqtt5Client) client).toAsync().disconnect());
                            break;
                        case MQTT_3_1_1:
                            disconnectFutures.add(((Mqtt3Client) client).toAsync().disconnect());
                            break;
                    }
                }
            }

            CompletableFuture.allOf(disconnectFutures.toArray(new CompletableFuture<?>[0]))
                    .join();
        }
    }

}
