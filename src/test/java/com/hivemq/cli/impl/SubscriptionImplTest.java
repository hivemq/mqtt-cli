package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SubscriptionImplTest {

    private Subscribe param;

    @Before
    public void setUp() throws Exception {
        param = new Subscribe();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void run() {
        SubscriptionImpl subscription = new SubscriptionImpl(param);
       //  subscription.run();
    }
}