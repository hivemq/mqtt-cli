package com.hivemq.cli.utils;

import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;

import java.io.*;

public class FileUtils {

    public static PrintWriter createFileAppender(@NotNull File file)  {

        OutputStream out = null;

        if ( file.exists() && !file.isDirectory() ) { // append to existing file
            try {
                out = new FileOutputStream(file, true);
            } catch (final FileNotFoundException e) {
                Logger.error("Could not open file: {} ", file.getName(), e.getMessage());
            }
        }
        else { // file has to be created
            file = new File(file.getName());
            try {
                out = new FileOutputStream(file);
            } catch (final FileNotFoundException e) {
                Logger.error("Could not create file: {} ", file.getName(), e.getMessage());
            }
        }

        return new PrintWriter(out);
    }
}
