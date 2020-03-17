package com.hivemq.cli.utils;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TopicUtils {

    public static @NotNull String generateTopicUUID() {
        final String uuid = UUID.randomUUID().toString();
        return uuid.replace("-","");
    }

    public static @NotNull String generateTopicUUID(final int maxLength) {
        if (maxLength == -1) return generateTopicUUID();
        else return generateTopicUUID().substring(0, maxLength);
    }
}
