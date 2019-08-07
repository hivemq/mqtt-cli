package com.hivemq.cli.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PasswordUtils {
    private static final boolean STARTED_IN_IDE = true;

    public static char[] readPassword(String... args) throws IOException {
        if (args.length > 1) throw new IllegalArgumentException();
        String promptMessage = "";
        if (args.length == 1) {
            promptMessage = args[0];
        }

        if (STARTED_IN_IDE) {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.println(promptMessage);
            return in.readLine().toCharArray();
        } else {
            return System.console().readPassword("[%s]", promptMessage);
        }
    }

}
