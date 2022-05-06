package com.hivemq.cli.graal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hivemq.cli.utils.json.JsonMqttPublish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class NativeMain {

    private static final @NotNull Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();

    /**
     * This
     *
     * @param args
     */
    public static void main(final @NotNull String... args) {
        Logger.info("Hello World!");
        final Mqtt3Publish sdfsd = Mqtt3Publish.builder().topic("sdfsd").build();
        final JsonMqttPublish jsonMqttPublish = new JsonMqttPublish(sdfsd, true);
        Logger.info(gson.toJson(jsonMqttPublish));
    }
}
