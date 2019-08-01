package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.mqtt.TestableMqttClientExecutor;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ConnectImplTest {

    private Connect param;

    @Before
    public void setUp() {
        param = new Connect();
        param.setHost("localhost");
        param.setPort(1883);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void runSuccess() {
        TestableMqttClientExecutor.getInstance().connect(param);
        assertNotNull(param.getKey());
        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());
        assertEquals(param.getHost(), client.getConfig().getServerHost());
        assertEquals(param.getPort(), client.getConfig().getServerPort());
        assertNotNull(client.getConfig().getClientIdentifier());

    }

    @Test
    public void runUseIdentifier() {
        param.setIdentifier("test");

        TestableMqttClientExecutor.getInstance().connect(param);

        assertNotNull(param.getKey());
        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());
        assertEquals(param.getIdentifier(), client.getConfig().getClientIdentifier().get().toString());

    }

    @Test
    public void runUseWill() {
        param.setWillTopic("test");
        param.setWillMessage(ByteBuffer.wrap("test".getBytes()));
        param.setWillQos(MqttQos.AT_MOST_ONCE);
        param.setWillRetain(false);

        TestableMqttClientExecutor.getInstance().connect(param);

        assertNotNull(param.getKey());
        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();
        assertEquals(param.getWillTopic(), connectMgs.getWillPublish().get().getTopic().toString());
    }


    @Test
    public void runUseWillMessageOnly() {
        param.setWillMessage(ByteBuffer.wrap("test".getBytes()));

        TestableMqttClientExecutor.getInstance().connect(param);

        assertNotNull(param.getKey());
        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());
        assertEquals(param.getIdentifier(), client.getConfig().getClientIdentifier().get().toString());
        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();

        assertFalse(connectMgs.getWillPublish().isPresent());
    }

    @Test
    public void runUseWillQosOnly() {
        param.setWillQos(MqttQos.AT_MOST_ONCE);

        TestableMqttClientExecutor.getInstance().connect(param);

        assertNotNull(param.getKey());
        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());
        assertEquals(param.getIdentifier(), client.getConfig().getClientIdentifier().get().toString());
        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();

        assertFalse(connectMgs.getWillPublish().isPresent());
    }

    @Test
    public void runUseWillRetainOnly() {
        param.setWillRetain(true);

        TestableMqttClientExecutor.getInstance().connect(param);

        assertNotNull(param.getKey());
        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());
        assertEquals(param.getIdentifier(), client.getConfig().getClientIdentifier().get().toString());
        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();

        assertFalse(connectMgs.getWillPublish().isPresent());
    }

    @Test
    public void runUseKeepAlive() {
        int expected = 120;

        param.setKeepAlive(expected);

        TestableMqttClientExecutor.getInstance().connect(param);

        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();

        assertEquals(expected, connectMgs.getKeepAlive());
    }

    @Test
    public void runUseSessionExpiryInterval() {
        long expected = 60;

        param.setSessionExpiryInterval(expected);
        TestableMqttClientExecutor.getInstance().connect(param);

        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();

        assertEquals(expected, connectMgs.getSessionExpiryInterval());
    }

    @Test
    public void runUseUser() {
        String expected = "user";

        param.setUser(expected);
        TestableMqttClientExecutor.getInstance().connect(param);

        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();

        assertTrue(connectMgs.getSimpleAuth().isPresent());
        assertFalse(connectMgs.getSimpleAuth().get().getPassword().isPresent());
        assertTrue(connectMgs.getSimpleAuth().get().getUsername().isPresent());
        assertEquals(expected,  connectMgs.getSimpleAuth().get().getUsername().get().toString());
    }

    @Test
    public void runUsePassword() {
        String expected = "password";

        param.setPassword(ByteBuffer.wrap(expected.getBytes()));
        TestableMqttClientExecutor.getInstance().connect(param);

        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();

        assertTrue(connectMgs.getSimpleAuth().isPresent());
        assertFalse(connectMgs.getSimpleAuth().get().getUsername().isPresent());
        assertTrue(connectMgs.getSimpleAuth().get().getPassword().isPresent());

        // Convert ByteBuffer to String
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(connectMgs.getSimpleAuth().get().getPassword().get());
        String actual = charBuffer.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void runUseUserAndPassword() {
        String expectedUser = "user";
        String expectedPassword = "password";

        param.setUser(expectedUser);
        param.setPassword(ByteBuffer.wrap(expectedPassword.getBytes()));
        param.setPassword(ByteBuffer.wrap(expectedPassword.getBytes()));

        TestableMqttClientExecutor.getInstance().connect(param);

        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();


        assertTrue(connectMgs.getSimpleAuth().isPresent());
        assertTrue(connectMgs.getSimpleAuth().get().getUsername().isPresent());
        assertTrue(connectMgs.getSimpleAuth().get().getPassword().isPresent());

        // Convert ByteBuffer to String
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(connectMgs.getSimpleAuth().get().getPassword().get());
        String actualPassword = charBuffer.toString();

        assertEquals(expectedUser, connectMgs.getSimpleAuth().get().getUsername().get().toString());
        assertEquals(expectedPassword, actualPassword);
    }

    @Test
    public void runUseNoUser() {
        param.setUser(null);
        TestableMqttClientExecutor.getInstance().connect(param);

        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();

        assertFalse(connectMgs.getSimpleAuth().isPresent());
    }

    @Test
    public void runUseNoPassword() {
        param.setPassword(null);
        TestableMqttClientExecutor.getInstance().connect(param);

        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();

        assertFalse(connectMgs.getSimpleAuth().isPresent());
    }

    @Test
    public void runUseCleanStart() {
        param.setCleanStart(true);

        TestableMqttClientExecutor.getInstance().connect(param);

        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();

        assertTrue(connectMgs.isCleanStart());
    }

    @Test
    public void runUseAuthenticationWithAttributes() {
        String expectedUser = "user";
        String expectedPassword = "password";

        param.setUser(expectedUser);
        param.setPassword(ByteBuffer.wrap(expectedPassword.getBytes()));
        param.setPassword(ByteBuffer.wrap(expectedPassword.getBytes()));
        param.setCleanStart(true);
        param.setKeepAlive(120);
        param.setSessionExpiryInterval(60);

        TestableMqttClientExecutor.getInstance().connect(param);

        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();


        assertTrue(connectMgs.getSimpleAuth().isPresent());
        assertTrue(connectMgs.getSimpleAuth().get().getUsername().isPresent());
        assertTrue(connectMgs.getSimpleAuth().get().getPassword().isPresent());

        // Convert ByteBuffer to String
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(connectMgs.getSimpleAuth().get().getPassword().get());
        String actualPassword = charBuffer.toString();

        assertEquals(expectedUser, connectMgs.getSimpleAuth().get().getUsername().get().toString());
        assertEquals(expectedPassword, actualPassword);
        assertEquals(param.isCleanStart(), connectMgs.isCleanStart());
        assertEquals(param.getKeepAlive(), connectMgs.getKeepAlive());
        assertEquals(param.getSessionExpiryInterval(), connectMgs.getSessionExpiryInterval());
    }



}