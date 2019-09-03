package com.hivemq.cli.utils;

import com.hivemq.cli.DefaultProperties;
import com.hivemq.client.mqtt.MqttVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesUtilsTest {

    private String pathToFullPropertiesFile;
    private String pathToPropertiesFile;
    private String pathToEmptyPropertiesFile;

    @BeforeEach
    void setUp() {
        PropertiesUtils.resetDefaultProperties();

        final URL fullPropertiesFile = getClass().getClassLoader().getResource("PropertiesUtils/test.properties");
        final URL propertiesFile = getClass().getClassLoader().getResource("PropertiesUtils/missing.properties");
        final URL emptyPropertiesFile = getClass().getClassLoader().getResource("PropertiesUtils/empty.properties");

        assertNotNull(fullPropertiesFile);
        assertNotNull(propertiesFile);
        assertNotNull(emptyPropertiesFile);

        pathToFullPropertiesFile = fullPropertiesFile.getPath();
        pathToPropertiesFile = propertiesFile.getPath();
        pathToEmptyPropertiesFile = emptyPropertiesFile.getPath();
    }

    @Test
    void getDefaultProperties() {
        final Properties defaultProperties = PropertiesUtils.getDefaultProperties();

        assertEquals(DefaultProperties.HOST, defaultProperties.getProperty("mqtt.host"));
        assertEquals(String.valueOf(DefaultProperties.PORT), defaultProperties.getProperty("mqtt.port"));
        assertEquals(DefaultProperties.MQTT_VERSION_STRING, defaultProperties.getProperty("mqtt.version"));
        assertEquals(DefaultProperties.CLIENT_PREFIX, defaultProperties.getProperty("client.prefix"));
        assertEquals(DefaultProperties.SHELL_DEBUG_LEVEL.name(), defaultProperties.getProperty("debug.level.shell"));
        assertEquals(DefaultProperties.LOGFILE_PATH, defaultProperties.getProperty("debug.logfile.path"));
        assertEquals(DefaultProperties.SUBSCRIBE_OUTPUT_FILE, defaultProperties.getProperty("client.subscribe.output"));
    }

    @Test
    void readDefaultProperties_NOT_FOUND() throws Exception {
        final Properties properties = PropertiesUtils.readDefaultProperties("/" + "noFile" + UUID.randomUUID() + ".properties");

        assertNull(properties);
    }

    @Test
    void readDefaultProperties_SUCCESS() throws Exception {
        final Properties properties = PropertiesUtils.readDefaultProperties(pathToFullPropertiesFile);

        assertNotNull(properties);
    }

    @Test
    void readDefaultProperties_MISSING_SUCCESS() throws Exception {
        final Properties properties = PropertiesUtils.readDefaultProperties(pathToPropertiesFile);

        assertEquals("broker.hivemq.com", properties.getProperty("mqtt.host"));
        assertEquals(String.valueOf(DefaultProperties.PORT), properties.getProperty("mqtt.port"));
        assertEquals("3", properties.getProperty("mqtt.version"));
        assertEquals(DefaultProperties.CLIENT_PREFIX, properties.getProperty("client.prefix"));
        assertEquals(PropertiesUtils.DEBUG_LEVEL.DEBUG.name(), properties.getProperty("debug.level.shell"));
        assertEquals(DefaultProperties.LOGFILE_PATH, properties.getProperty("debug.logfile.path"));
        assertEquals(DefaultProperties.SUBSCRIBE_OUTPUT_FILE, properties.getProperty("client.subscribe.output"));
    }

    @Test
    void readDefaultProperties_EMPTY_SUCCESS() throws Exception {
        final Properties properties = PropertiesUtils.readDefaultProperties(pathToEmptyPropertiesFile);

        assertEquals(DefaultProperties.HOST, properties.getProperty("mqtt.host"));
        assertEquals(String.valueOf(DefaultProperties.PORT), properties.getProperty("mqtt.port"));
        assertEquals(DefaultProperties.MQTT_VERSION_STRING, properties.getProperty("mqtt.version"));
        assertEquals(DefaultProperties.CLIENT_PREFIX, properties.getProperty("client.prefix"));
        assertEquals(DefaultProperties.SHELL_DEBUG_LEVEL.name(), properties.getProperty("debug.level.shell"));
        assertEquals(DefaultProperties.LOGFILE_PATH, properties.getProperty("debug.logfile.path"));
        assertEquals(DefaultProperties.SUBSCRIBE_OUTPUT_FILE, properties.getProperty("client.subscribe.output"));
    }

    @Test
    void setDefaultProperties_ALL_SUCCESS() throws Exception {
        final Properties properties = PropertiesUtils.readDefaultProperties(pathToFullPropertiesFile);

        PropertiesUtils.setDefaultProperties(properties);

        assertEquals("broker.hivemq.com", PropertiesUtils.DEFAULT_HOST);
        assertEquals(1883, PropertiesUtils.DEFAULT_PORT);
        assertEquals(MqttVersion.MQTT_3_1_1, PropertiesUtils.DEFAULT_MQTT_VERSION);
        assertEquals("HiveMQClient", PropertiesUtils.DEFAULT_CLIENT_PREFIX);
        assertEquals(PropertiesUtils.DEBUG_LEVEL.DEBUG, PropertiesUtils.DEFAULT_SHELL_DEBUG_LEVEL);
        assertEquals("/etc/.hivemq-cli/logs", PropertiesUtils.DEFAULT_LOGFILE_PATH);
        assertEquals("file.txt", PropertiesUtils.DEFAULT_SUBSCRIBE_OUTPUT_FILE);

        assertEquals(properties, PropertiesUtils.DEFAULT_PROPERTIES);
    }

    @Test
    void setDefaultProperties_MISSING_SUCCESS() throws Exception {
        final Properties properties = PropertiesUtils.readDefaultProperties(pathToPropertiesFile);

        PropertiesUtils.setDefaultProperties(properties);

        assertEquals("broker.hivemq.com", PropertiesUtils.DEFAULT_HOST);
        assertEquals(1883, PropertiesUtils.DEFAULT_PORT);
        assertEquals(MqttVersion.MQTT_3_1_1, PropertiesUtils.DEFAULT_MQTT_VERSION);
        assertEquals("hmqClient", PropertiesUtils.DEFAULT_CLIENT_PREFIX);
        assertEquals(PropertiesUtils.DEBUG_LEVEL.DEBUG, PropertiesUtils.DEFAULT_SHELL_DEBUG_LEVEL);
        assertEquals(DefaultProperties.LOGFILE_PATH, PropertiesUtils.DEFAULT_LOGFILE_PATH);
        assertEquals(DefaultProperties.SUBSCRIBE_OUTPUT_FILE, PropertiesUtils.DEFAULT_SUBSCRIBE_OUTPUT_FILE);

        assertEquals(properties, PropertiesUtils.DEFAULT_PROPERTIES);
    }
}