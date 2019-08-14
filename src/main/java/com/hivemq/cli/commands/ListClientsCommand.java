package com.hivemq.cli.commands;

import com.hivemq.cli.mqtt.ClientCache;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientConfig;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Inject;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@CommandLine.Command(name = "ls",
        description = "List all connected clients with their respective identifieres",
        mixinStandardHelpOptions = true)
public class ListClientsCommand implements Runnable {

    private final MqttClientExecutor mqttClientExecutor;

    @Inject
    ListClientsCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        this.mqttClientExecutor = mqttClientExecutor;
    }

    @CommandLine.Option(names = {"-t", "--time"}, defaultValue = "false", description = "Sort clients ordered by their creation time")
    private boolean sortByTime;

    @CommandLine.Option(names = {"-a", "--all"}, defaultValue = "false", description = "List clients with detailed client inforamtion")
    private boolean outputDetailedClientInformation;

    @CommandLine.Option(names = {"-at", "-ta"})
    private void setOptionsTrue(final boolean allTrue) {
        sortByTime = allTrue;
        outputDetailedClientInformation = allTrue;
    }


    @Override
    public void run() {

        ClientCache<String, Mqtt5AsyncClient> clientCache = mqttClientExecutor.getClientCache();
        Map<String, LocalTime> creationTimes = mqttClientExecutor.getClientCreationTimes();
        Set<String> clientKeys = clientCache.keySet();

        Comparator<String> comparator = (s, t1) -> {
            String clientID1 = clientCache.get(s).getConfig().getClientIdentifier().toString();
            String clientID2 = clientCache.get(t1).getConfig().getClientIdentifier().toString();
            return clientID1.compareTo(clientID2);
        };

        if (sortByTime) {
            comparator = Comparator.comparing(creationTimes::get);
        }
        TreeMap<String, MqttClient> sortedClients = new TreeMap<>(comparator);

        for (String key :
                clientKeys) {
            MqttClient client = clientCache.get(key);
            sortedClients.put(key, client);
        }


        String outputFormat = "%-20s %-25s\n";

        if (outputDetailedClientInformation) {
            outputFormat = "%-25s %-20s %-20s %-10s %-25s %-15s %-15s\n";
            System.out.printf(outputFormat,
                    "Created-At",
                    "Client-ID",
                    "Host",
                    "Port",
                    "Server-Address",
                    "MQTT version",
                    "SSL"
            );
        } else {
            System.out.printf(outputFormat,
                    "Client-ID",
                    "Server-Address");
        }

        for (Map.Entry<String, MqttClient> entry : sortedClients.entrySet()) {

            String clientKey = entry.getKey();
            MqttClient client = entry.getValue();
            MqttClientConfig config = client.getConfig();
            LocalTime timestamp = creationTimes.get(clientKey);

            if (outputDetailedClientInformation) {
                System.out.printf(outputFormat,
                        timestamp,
                        config.getClientIdentifier().orElse(null),
                        config.getServerHost(),
                        config.getServerPort(),
                        config.getServerAddress(),
                        config.getMqttVersion(),
                        config.getSslConfig().map((m) -> "true").orElse("false")
                );
            } else {

                System.out.printf(outputFormat,
                        config.getClientIdentifier().orElse(null),
                        config.getServerAddress());

            }
        }

    }
}
