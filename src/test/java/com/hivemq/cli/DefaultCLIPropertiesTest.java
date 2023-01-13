/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.cli;

import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tinylog.Level;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultCLIPropertiesTest {

    private @NotNull Path pathToTmpDir;
    private @NotNull Path pathToNoProperties;
    private @NotNull Path pathToEmptyProperties;
    private @NotNull Path pathToOverrideProperties;

    @BeforeEach
    void setUp(@TempDir final @NotNull Path pathToTmpDir, @TempDir final @NotNull Path pathToNoProperties)
            throws IOException, URISyntaxException {
        this.pathToTmpDir = pathToTmpDir;
        this.pathToNoProperties = pathToNoProperties;
        System.setProperty("user.home", pathToTmpDir.toString());
        final File mqttDir = pathToTmpDir.resolve(".mqtt-cli").toFile();
        assertTrue(mqttDir.createNewFile());

        final URL emptyPropertiesResource = getClass().getResource("/PropertyFiles/empty.properties");
        final URL overridePropertiesResource = getClass().getResource("/PropertyFiles/override.properties");
        assertNotNull(emptyPropertiesResource);
        assertNotNull(overridePropertiesResource);
        pathToEmptyProperties = Paths.get(emptyPropertiesResource.toURI());
        pathToOverrideProperties = Paths.get(overridePropertiesResource.toURI());
    }

    @Test
    void noPropertyFile() throws Exception {
        final Path missingProperties = pathToNoProperties.resolve("missing.properties");
        final DefaultCLIProperties defaultCLIProperties = new DefaultCLIProperties(missingProperties);

        defaultCLIProperties.init();

        assertTrue(missingProperties.toFile().exists());
        assertEquals(MqttVersion.MQTT_5_0, defaultCLIProperties.getMqttVersion());
        assertEquals("localhost", defaultCLIProperties.getHost());
        assertEquals(1883, defaultCLIProperties.getPort());
        assertEquals(Level.DEBUG, defaultCLIProperties.getLogfileDebugLevel());
        assertEquals("mqtt", defaultCLIProperties.getClientPrefix());
        assertEquals(pathToTmpDir.resolve(".mqtt-cli").resolve("logs"), defaultCLIProperties.getLogfilePath());
        assertNull(defaultCLIProperties.getClientSubscribeOutputFile());
        assertNull(defaultCLIProperties.getUsername());
        assertNull(defaultCLIProperties.getPassword());
        assertNull(defaultCLIProperties.getClientCertificateChain());
        assertNull(defaultCLIProperties.getServerCertificateChain());
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
        assertEquals(pathToTmpDir.resolve(".mqtt-cli").resolve("logs"), defaultCLIProperties.getLogfilePath());
        assertNull(defaultCLIProperties.getClientSubscribeOutputFile());
        assertNull(defaultCLIProperties.getUsername());
        assertNull(defaultCLIProperties.getPassword());
        assertNull(defaultCLIProperties.getClientCertificateChain());
        assertNull(defaultCLIProperties.getServerCertificateChain());
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
        assertEquals(Paths.get("some_folder", ".mqtt-cli", "logs"), defaultCLIProperties.getLogfilePath());
        assertNull(defaultCLIProperties.getClientSubscribeOutputFile());
        assertEquals("mqtt", defaultCLIProperties.getUsername());
        assertEquals(ByteBuffer.wrap("password".getBytes()), defaultCLIProperties.getPassword());
        assertNull(defaultCLIProperties.getClientCertificateChain());
        assertNull(defaultCLIProperties.getServerCertificateChain());
        assertNull(defaultCLIProperties.getClientPrivateKey());
    }
}
