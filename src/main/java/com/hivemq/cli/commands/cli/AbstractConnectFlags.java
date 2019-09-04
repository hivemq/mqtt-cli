package com.hivemq.cli.commands.cli;

import com.hivemq.cli.commands.AbstractCommonFlags;
import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.converters.UserPropertiesConverter;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

public class AbstractConnectFlags extends AbstractCommonFlags implements Connect {

    @CommandLine.Option(names = {"-Ce", "--connectSessionExpiry"}, converter = UnsignedIntConverter.class, description = "The lifetime of the session of the connected client", order = 2)
    @Nullable
    private Long connectSessionExpiryInterval;

    @CommandLine.Option(names = {"-Cup", "--connectUserProperties"}, converter = UserPropertiesConverter.class, description = "The user properties of the connect message (usage: 'Key=Value', 'Key1=Value1|Key2=Value2)'", order = 2)
    @Nullable
    private Mqtt5UserProperties connectUserProperties;


    String connectOptions() {
        return commonOptions() +
                ", sessionExpiryInterval= " + connectSessionExpiryInterval +
                ", userProperties=" + connectUserProperties +
                ", " + connectRestrictionOptions();

    }

    @Override
    public void logUnusedOptions() {
        super.logUnusedOptions();
        if (getVersion() == MqttVersion.MQTT_3_1_1) {
            if (connectSessionExpiryInterval != null) {
                Logger.warn("Connect session expiry interval was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }

            if (connectUserProperties != null) {
                Logger.warn("Connect user properties were set but are unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
        }
    }

    @Nullable
    public Long getConnectSessionExpiryInterval() {
        return connectSessionExpiryInterval;
    }

    public void setConnectSessionExpiryInterval(@Nullable final Long connectSessionExpiryInterval) {
        this.connectSessionExpiryInterval = connectSessionExpiryInterval;
    }

    @Nullable
    public Mqtt5UserProperties getConnectUserProperties() {
        return connectUserProperties;
    }

    public void setConnectUserProperties(@Nullable final Mqtt5UserProperties connectUserProperties) {
        this.connectUserProperties = connectUserProperties;
    }

}
