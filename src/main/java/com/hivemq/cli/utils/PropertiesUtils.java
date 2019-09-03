package com.hivemq.cli.utils;

import com.hivemq.cli.converters.MqttVersionConverter;
import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.util.Properties;
import java.util.function.Consumer;

public class PropertiesUtils {

    // Default values

    public enum DEBUG_LEVEL {
        INFO, DEBUG, VERBOSE;
    }

    public static final String PROPERTIES_FILE_PATH = getPropertiesPath();

    public static MqttVersion DEFAULT_MQTT_VERSION = MqttVersion.MQTT_5_0;

    public static String DEFAULT_MQTT_VERSION_STRING = "5";

    public static String DEFAULT_HOST = "localhost";

    public static int DEFAULT_PORT = 1883;

    public static DEBUG_LEVEL DEFAULT_SHELL_DEBUG_LEVEL = DEBUG_LEVEL.VERBOSE;

    public static String DEFAULT_CLIENT_PREFIX = "hmqClient";

    public static String DEFAULT_LOGFILE_PATH = getLogfilePath();

    public static String DEFAULT_SUBSCRIBE_OUTPUT_FILE;

    public static Properties DEFAULT_PROPERTIES = getDefaultProperties();


    private static String getPropertiesPath() {
        return System.getProperty("user.home") + File.separator + ".hivemq-cli" + File.separator + "config.properties";
    }

    private static String getLogfilePath() {
        return System.getProperty("user.home") + File.separator + ".hivemq-cli" + File.separator + "logs" + File.separator;
    }

    public static Properties getDefaultProperties() {
        final Properties properties = new Properties();

        properties.setProperty("mqtt.version", DEFAULT_MQTT_VERSION_STRING);
        properties.setProperty("mqtt.host", DEFAULT_HOST);
        properties.setProperty("mqtt.port", String.valueOf(DEFAULT_PORT));
        properties.setProperty("debug.level.shell", DEFAULT_SHELL_DEBUG_LEVEL.name());
        properties.setProperty("debug.logfile.path", DEFAULT_LOGFILE_PATH);
        properties.setProperty("client.prefix", DEFAULT_CLIENT_PREFIX);
        //properties.setProperty("client.subscriber.output" ...

        return properties;
    }


    public static Properties createDefaultPropertiesFile(final @NotNull Properties properties, final @NotNull String propertiesPath) throws IOException {

        final File file = new File(propertiesPath);

        file.getParentFile().mkdirs();

        file.createNewFile();

        try (final OutputStream output = new FileOutputStream(file)) {
            properties.store(output, null);
        }
        catch (final IOException e) {
            throw (e);
        }
        return properties;
    }

    public static @Nullable Properties readDefaultProperties(final @NotNull String propertiesPath) throws Exception {
        final Properties properties = new Properties(DEFAULT_PROPERTIES);

        try (final InputStream input = new FileInputStream(propertiesPath)) {

            properties.load(input);

        }
        catch (final FileNotFoundException e) {
            return null;
        }
        catch (final Exception e) {
            throw (e);
        }

        return properties;
    }

    public static void setDefaultProperties(final @NotNull Properties properties) throws Exception {
        DEFAULT_MQTT_VERSION = new MqttVersionConverter().convert(properties.getProperty("mqtt.version"));
        DEFAULT_HOST = properties.getProperty("mqtt.host");
        DEFAULT_PORT = Integer.valueOf(properties.getProperty("mqtt.port"));
        DEFAULT_SHELL_DEBUG_LEVEL = DEBUG_LEVEL.valueOf(properties.getProperty("debug.level.shell"));
        DEFAULT_LOGFILE_PATH = properties.getProperty("debug.logfile.path");
        DEFAULT_CLIENT_PREFIX = properties.getProperty("client.prefix");
        DEFAULT_SUBSCRIBE_OUTPUT_FILE = properties.getProperty("client.subscribe.output");

        DEFAULT_PROPERTIES = properties;
    }

}
