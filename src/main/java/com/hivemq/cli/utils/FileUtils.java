/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hivemq.cli.utils;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class FileUtils {

    public static PrintWriter createFileAppender(@NotNull File file)  {

        OutputStream out = null;

        if ( file.exists() && !file.isDirectory() ) { // append to existing file
            try {
                out = new FileOutputStream(file, true);
            } catch (final FileNotFoundException e) {
                Logger.error(e,"Could not open file ({}) ", file.getName(), e.getMessage());
            }
        }
        else { // file has to be created
            file = new File(file.getName());
            try {
                out = new FileOutputStream(file);
            } catch (final FileNotFoundException e) {
                Logger.error(e, "Could not create file ({}) ", file.getName(), e.getMessage());
            }
        }

        return new PrintWriter(out);
    }
}
