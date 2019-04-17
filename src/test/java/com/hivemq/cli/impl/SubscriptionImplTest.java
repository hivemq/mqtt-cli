package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Subscribe;

public class SubscriptionImplTest {

    // private Mqtt5Client client;
    private Subscribe param;

    public void run() {
        // client = Mqtt5Client.builder().build();
        param = new Subscribe();
        SubscriptionImpl subscription = new SubscriptionImpl(param); //, client);
    }
}