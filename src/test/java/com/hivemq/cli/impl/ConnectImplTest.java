package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.commands.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConnectImplTest {

    private Connect param;

    @Before
    public void setUp() throws Exception {
        param = new Connect();
        param.setHost("localhost");
        param.setPort(1883);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void runSuccess() {
        ConnectionImpl.get((Connect) param).run();
    }
}