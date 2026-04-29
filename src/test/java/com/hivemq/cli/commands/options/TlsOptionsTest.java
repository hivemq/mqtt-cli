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

package com.hivemq.cli.commands.options;

import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.ioc.MqttCLI;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.nio.file.Path;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TlsOptionsTest {

    @BeforeEach
    void setUp(@TempDir final @NotNull Path tempDir) throws Exception {
        final DefaultCLIProperties defaultCLIProperties = new DefaultCLIProperties(tempDir.resolve("config.properties"));

        final MqttCLI mqttCLI = mock(MqttCLI.class);
        when(mqttCLI.defaultCLIProperties()).thenReturn(defaultCLIProperties);
        MqttCLIMain.MQTT_CLI = mqttCLI;
    }

    @AfterEach
    void tearDown() {
        MqttCLIMain.MQTT_CLI = null;
    }

    @Test
    void buildSslConfig_noTlsOptions_returnsNull() throws Exception {
        final TlsOptions tlsOptions = new TlsOptions();

        assertNull(tlsOptions.buildSslConfig());
    }

    @Test
    void buildSslConfig_secureOnly_keepsDefaultTrustAndHostnameValidation() throws Exception {
        final TlsOptions tlsOptions = new TlsOptions();
        new CommandLine(tlsOptions).parseArgs("--secure");

        final MqttClientSslConfig sslConfig = tlsOptions.buildSslConfig();

        assertNotNull(sslConfig);
        assertFalse(sslConfig.getTrustManagerFactory().isPresent());
        assertFalse(sslConfig.getHostnameVerifier().isPresent());
    }

    @Test
    void buildSslConfig_insecure_usesTrustAllAndHostnameVerifier() throws Exception {
        final TlsOptions tlsOptions = new TlsOptions();
        tlsOptions.setInsecure(true);

        final MqttClientSslConfig sslConfig = tlsOptions.buildSslConfig();

        assertNotNull(sslConfig);
        assertTrue(sslConfig.getHostnameVerifier().orElseThrow().verify("untrusted.example", null));
        final TrustManager trustManager =
                sslConfig.getTrustManagerFactory().orElseThrow().getTrustManagers()[0];
        assertTrue(trustManager instanceof X509TrustManager);
        assertDoesNotThrow(() -> ((X509TrustManager) trustManager).checkServerTrusted(new X509Certificate[0], "RSA"));
    }

    @Test
    void tlsOptions_doesNotExposeInsecureCliOptionDirectly() {
        final CommandLine commandLine = new CommandLine(new TlsOptions());

        assertFalse(commandLine.getCommandSpec().optionsMap().containsKey("--insecure"));
    }
}
