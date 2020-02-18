package com.hivemq.cli.mqtt.test;

public class QosTestResult {
    private final int receivedPublishes;
    private final long timeToReceivePublishes;

    public QosTestResult(final int receivedPublishes, final long timeToReceivePublishes) {
        this.receivedPublishes = receivedPublishes;
        this.timeToReceivePublishes = timeToReceivePublishes;
    }

    public int getReceivedPublishes() { return receivedPublishes; }

    public long getTimeToReceivePublishes() { return timeToReceivePublishes; }
}
