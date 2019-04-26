package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Subscribe;
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
        SubscriptionImpl.get(param);
        //  subscription.run();
    }
}