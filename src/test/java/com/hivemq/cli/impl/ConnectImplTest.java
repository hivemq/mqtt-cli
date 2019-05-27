package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.mqtt.TestableMqttClientExecutor;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

        TestableMqttClientExecutor.getInstance().connect(param);

        assertNotNull(param.getKey());
        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();
        assertEquals(param.getWillTopic(), connectMgs.getWillPublish().get().getTopic().toString());
    }


    @Test
    public void runUseWillWrong() {
        param.setWillMessage("test");

        TestableMqttClientExecutor.getInstance().connect(param);

        assertNotNull(param.getKey());
        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));
        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());
        assertEquals(param.getIdentifier(), client.getConfig().getClientIdentifier().get().toString());
        Mqtt5Connect connectMgs = TestableMqttClientExecutor.getInstance().getConnectMgs();

        assertFalse(connectMgs.getWillPublish().isPresent());


    }

}