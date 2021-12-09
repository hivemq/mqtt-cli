package com.hivemq.cli.utils;

import org.tinylog.configuration.Configuration;

import java.lang.reflect.Field;

/**
 * @author Christoph Schäbel
 */
public class TestLoggerUtils {

    /**
     * Used to reset the logger after tests
     */
    public static void resetLogger() {
        final Field frozen;
        try {
            frozen = Configuration.class.getDeclaredField("frozen");
            frozen.setAccessible(true);
            frozen.set(null, false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

}
