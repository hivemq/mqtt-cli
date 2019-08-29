package com.hivemq.cli;

import com.hivemq.cli.ioc.DaggerHiveMQCLI;
import com.hivemq.cli.mqtt.ClientCache;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.SizePolicy;
import org.pmw.tinylog.writers.ConsoleWriter;
import org.pmw.tinylog.writers.RollingFileWriter;
import picocli.CommandLine;

import javax.inject.Inject;
import java.security.Security;
import java.util.Set;

public class HiveMQCLIMain {

    public static CommandLine.Help.ColorScheme colorScheme = new CommandLine.Help.ColorScheme.Builder(CommandLine.Help.Ansi.ON)
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

        Runtime.getRuntime().addShutdownHook(new disconnectAllClientsTask());

        final int exitCode = commandLine.execute(args);

        System.exit(exitCode);

    }

    private static CommandLine setupCommandLine() {
        final CommandLine commandLine = DaggerHiveMQCLI.create().commandLine();

        commandLine.setColorScheme(colorScheme);

        Configurator.defaultConfig()
                .writer(new ConsoleWriter())
                .formatPattern("Client {context:identifier}: {message}")
                .level(Level.INFO)
                .activate();
        commandLine.setUsageHelpWidth(CLI_WIDTH);

        return commandLine;
    }

    private static class disconnectAllClientsTask extends Thread {

        @Override
        public void run() {
            final ClientCache<String, MqttClient> cache = MqttClientExecutor.getClientCache();
            cache.setVerbose(false);
            final Set<String> keys = cache.keySet();

            for (final String key : keys) {
                final MqttClient client = cache.get(key);
                switch (client.getConfig().getMqttVersion()) {
                    case MQTT_5_0:
                        ((Mqtt5Client) client).toAsync().disconnect().join();
                        break;
                    case MQTT_3_1_1:
                        ((Mqtt3Client) client).toAsync().disconnect().join();
                        break;
                }
            }
        }
    }

}
