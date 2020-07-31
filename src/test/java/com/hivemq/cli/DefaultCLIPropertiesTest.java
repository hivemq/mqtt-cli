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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tinylog.Level;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultCLIPropertiesTest {

    @TempDir static Path pathToTmpDir;
    private static String pathToPropertiesDir;
    private static String pathToEmptyProperties;
    private static String pathToOverrideProperties;
    private static String pathToNoProperties;

    @BeforeAll
    static void setUp() throws IOException {
        final ClassLoader classLoader = DefaultCLIPropertiesTest.class.getClassLoader();
        System.setProperty("user.home", pathToTmpDir.toString());
        File mqttDir = new File(pathToTmpDir.toString() + "/.mqtt-cli");
        mqttDir.createNewFile();
        pathToPropertiesDir = classLoader.getResource("PropertyFiles").getPath();
        pathToEmptyProperties = pathToPropertiesDir + "/empty.properties";
        pathToOverrideProperties = pathToPropertiesDir + "/override.properties";
        pathToNoProperties = pathToPropertiesDir + "/" + UUID.randomUUID().toString();
    }

    @AfterAll
    static void tearDown() {
        File createdFile = new File(pathToNoProperties);
        createdFile.delete();
    }

    @Test
    void noPropertyFile() throws Exception {
        final DefaultCLIProperties defaultCLIProperties = new DefaultCLIProperties(pathToNoProperties);

        defaultCLIProperties.init();

        assertTrue(new File(pathToNoProperties).exists());

        assertEquals(MqttVersion.MQTT_5_0, defaultCLIProperties.getMqttVersion());
        assertEquals("localhost", defaultCLIProperties.getHost());
        assertEquals(1883, defaultCLIProperties.getPort());
        assertEquals(Level.DEBUG, defaultCLIProperties.getLogfileDebugLevel());
        assertEquals("mqtt", defaultCLIProperties.getClientPrefix());
        assertEquals(pathToTmpDir + "/.mqtt-cli/logs/", defaultCLIProperties.getLogfilePath());
        assertNull(defaultCLIProperties.getClientSubscribeOutputFile());
        assertNull(defaultCLIProperties.getUsername());
        assertNull(defaultCLIProperties.getPassword());
        assertNull(defaultCLIProperties.getClientCertificate());
        assertNull(defaultCLIProperties.getServerCertificate());
        assertNull(defaultCLIProperties.getClientPrivateKey());
    }

    @Test
    void emptyPropertyFile() throws Exception {
        final DefaultCLIProperties defaultCLIProperties = new DefaultCLIProperties(pathToEmptyProperties);
        defaultCLIProperties.init();

        assertEquals(MqttVersion.MQTT_5_0, defaultCLIProperties.getMqttVersion());
        assertEquals("localhost", defaultCLIProperties.getHost());
        assertEquals(1883, defaultCLIProperties.getPort());
        assertEquals(Level.DEBUG, defaultCLIProperties.getLogfileDebugLevel());
        assertEquals("mqtt", defaultCLIProperties.getClientPrefix());
        assertEquals(pathToTmpDir + "/.mqtt-cli/logs/", defaultCLIProperties.getLogfilePath());
        assertNull(defaultCLIProperties.getClientSubscribeOutputFile());
        assertNull(defaultCLIProperties.getUsername());
        assertNull(defaultCLIProperties.getPassword());
        assertNull(defaultCLIProperties.getClientCertificate());
        assertNull(defaultCLIProperties.getServerCertificate());
        assertNull(defaultCLIProperties.getClientPrivateKey());
    }

    @Test
    void overridePropertyFile() throws Exception {
        final DefaultCLIProperties defaultCLIProperties = new DefaultCLIProperties(pathToOverrideProperties);
        defaultCLIProperties.init();

        assertEquals(MqttVersion.MQTT_3_1_1, defaultCLIProperties.getMqttVersion());
        assertEquals("broker.hivemq.com", defaultCLIProperties.getHost());
        assertEquals(1884, defaultCLIProperties.getPort());
        assertEquals(Level.TRACE, defaultCLIProperties.getLogfileDebugLevel());
        assertEquals("testprefix", defaultCLIProperties.getClientPrefix());
        assertEquals("/.mqtt-cli/logs", defaultCLIProperties.getLogfilePath());
        assertNull(defaultCLIProperties.getClientSubscribeOutputFile());
        assertEquals("mqtt" , defaultCLIProperties.getUsername());
        assertEquals(ByteBuffer.wrap("password".getBytes()), defaultCLIProperties.getPassword());
        assertNull(defaultCLIProperties.getClientCertificate());
        assertNull(defaultCLIProperties.getServerCertificate());
        assertNull(defaultCLIProperties.getClientPrivateKey());
    }

    @Test
    void directoryPath() {
        final DefaultCLIProperties defaultCLIProperties = new DefaultCLIProperties(pathToPropertiesDir);

        assertThrows(IllegalArgumentException.class, defaultCLIProperties::init);
    }

}