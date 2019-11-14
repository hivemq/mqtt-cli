/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hivemq.cli;

import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pmw.tinylog.Level;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCLIPropertiesTest {

    private static String propertyHomes;
    private static String realUserHome;

    @BeforeAll
    static void setUp() {
        final ClassLoader classLoader = DefaultCLIPropertiesTest.class.getClassLoader();
        propertyHomes = classLoader.getResource("PropertyHomes").getPath();
        realUserHome = System.getProperty("user.home");
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.home", realUserHome);
    }

    @AfterAll()
    static void tearDownAll() {
        final ClassLoader classLoader = DefaultCLIPropertiesTest.class.getClassLoader();
        final URL defaultPropertyPath = classLoader.getResource("PropertyHomes/home3/.mqtt-cli/config.properties");
        if (defaultPropertyPath != null) {
            final File defaultPropertyFile = new File(defaultPropertyPath.getFile());
            defaultPropertyFile.delete();
        }

    }

    @Test
    void noPropertyFile() throws Exception {
        final String fullHomePath = setupHomePath("home1");
        final DefaultCLIProperties defaultCLIProperties = new DefaultCLIProperties();

        assertThrows(IOException.class, defaultCLIProperties::readFromFile);

        assertEquals(MqttVersion.MQTT_5_0, defaultCLIProperties.getMqttVersion());
        assertEquals("localhost", defaultCLIProperties.getHost());
        assertEquals(1883, defaultCLIProperties.getPort());
        assertEquals(Level.TRACE, defaultCLIProperties.getShellDebugLevel());
        assertEquals("mqttClient", defaultCLIProperties.getClientPrefix());
        assertEquals(fullHomePath + "/.mqtt-cli/logs/", defaultCLIProperties.getLogfilePath());
        assertNull(defaultCLIProperties.getClientSubscribeOutputFile());
        assertNull(defaultCLIProperties.getUsername());
        assertNull(defaultCLIProperties.getPassword());
        assertNull(defaultCLIProperties.getClientCertificate());
        assertNull(defaultCLIProperties.getServerCertificate());
        assertNull(defaultCLIProperties.getClientPrivateKey());
    }

    @Test
    void emptyPropertyFile() throws Exception {
        final String fullHomePath = setupHomePath("home2");
        final DefaultCLIProperties defaultCLIProperties = new DefaultCLIProperties();
        defaultCLIProperties.readFromFile();

        assertEquals(MqttVersion.MQTT_5_0, defaultCLIProperties.getMqttVersion());
        assertEquals("localhost", defaultCLIProperties.getHost());
        assertEquals(1883, defaultCLIProperties.getPort());
        assertEquals(Level.TRACE, defaultCLIProperties.getShellDebugLevel());
        assertEquals("mqttClient", defaultCLIProperties.getClientPrefix());
        assertEquals(fullHomePath + "/.mqtt-cli/logs/", defaultCLIProperties.getLogfilePath());
        assertNull(defaultCLIProperties.getClientSubscribeOutputFile());
        assertNull(defaultCLIProperties.getUsername());
        assertNull(defaultCLIProperties.getPassword());
        assertNull(defaultCLIProperties.getClientCertificate());
        assertNull(defaultCLIProperties.getServerCertificate());
        assertNull(defaultCLIProperties.getClientPrivateKey());
    }

    @Test
    void defaultPropertyFile() throws Exception {
        final String fullHomePath = setupHomePath("home3");
        final DefaultCLIProperties defaultCLIProperties = new DefaultCLIProperties();
        defaultCLIProperties.createFile();
        defaultCLIProperties.readFromFile();

        assertEquals(MqttVersion.MQTT_5_0, defaultCLIProperties.getMqttVersion());
        assertEquals("localhost", defaultCLIProperties.getHost());
        assertEquals(1883, defaultCLIProperties.getPort());
        assertEquals(Level.TRACE, defaultCLIProperties.getShellDebugLevel());
        assertEquals("mqttClient", defaultCLIProperties.getClientPrefix());
        assertEquals(fullHomePath + "/.mqtt-cli/logs/", defaultCLIProperties.getLogfilePath());
        assertNull(defaultCLIProperties.getClientSubscribeOutputFile());
        assertNull(defaultCLIProperties.getUsername());
        assertNull(defaultCLIProperties.getPassword());
        assertNull(defaultCLIProperties.getClientCertificate());
        assertNull(defaultCLIProperties.getServerCertificate());
        assertNull(defaultCLIProperties.getClientPrivateKey());
    }

    @Test
    void overridePropertyFile() throws Exception {
        final String fullHomePath = setupHomePath("home4");
        final DefaultCLIProperties defaultCLIProperties = new DefaultCLIProperties();
        defaultCLIProperties.readFromFile();

        assertEquals(MqttVersion.MQTT_3_1_1, defaultCLIProperties.getMqttVersion());
        assertEquals("broker.hivemq.com", defaultCLIProperties.getHost());
        assertEquals(1884, defaultCLIProperties.getPort());
        assertEquals(Level.DEBUG, defaultCLIProperties.getShellDebugLevel());
        assertEquals("testprefix", defaultCLIProperties.getClientPrefix());
        assertEquals("/.mqtt-cli/logs", defaultCLIProperties.getLogfilePath());
        assertNull(defaultCLIProperties.getClientSubscribeOutputFile());
        assertEquals("mqtt" , defaultCLIProperties.getUsername());
        assertEquals(ByteBuffer.wrap("password".getBytes()), defaultCLIProperties.getPassword());
        assertNull(defaultCLIProperties.getClientCertificate());
        assertNull(defaultCLIProperties.getServerCertificate());
        assertNull(defaultCLIProperties.getClientPrivateKey());
    }

    String setupHomePath(final @NotNull String homeDirName) {
        final String homePath = propertyHomes + "/" + homeDirName;
        System.setProperty("user.home", homePath);
        return homePath;
    }
}