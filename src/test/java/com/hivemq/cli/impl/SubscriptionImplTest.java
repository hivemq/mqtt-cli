package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.mqtt.TestableMqttClientExecutor;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class SubscriptionImplTest {

    private Subscribe param;

    @Before
    public void setUp() {
        param = new Subscribe();
        param.setHost("localhost");
        param.setPort(1883);
    }

    @After
    public void tearDown() {

    }

    @Ignore
    @Test
    public void runSuccess() {
        param.setTopics(new String[]{"/"});
        TestableMqttClientExecutor.getInstance().subscribe(param);

        assertNotNull(param.getKey());
        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));

        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        assertEquals(param.getHost(), client.getConfig().getServerHost());
        assertEquals(param.getPort(), client.getConfig().getServerPort());
        assertNotNull(client.getConfig().getClientIdentifier());

        assertEquals(param.getTopics()[0], TestableMqttClientExecutor.getInstance().getSubscribeTopic().get(0));


    }

    @Ignore
    @Test
    public void runSuccess2() {
        param.setTopics(new String[]{"muster", "test"});
        TestableMqttClientExecutor.getInstance().subscribe(param);

        assertNotNull(param.getKey());
        assertTrue("cached element", TestableMqttClientExecutor.getInstance().getClientCache().hasKey(param.getKey()));

        Mqtt5AsyncClient client = TestableMqttClientExecutor.getInstance().getClientCache().get(param.getKey());

        assertEquals(param.getHost(), client.getConfig().getServerHost());
        assertEquals(param.getPort(), client.getConfig().getServerPort());
        assertNotNull(client.getConfig().getClientIdentifier());

        assertEquals(param.getTopics()[0], TestableMqttClientExecutor.getInstance().getSubscribeTopic().get(0));

        assertEquals(param.getTopics()[1], TestableMqttClientExecutor.getInstance().getSubscribeTopic().get(1));
    }

}