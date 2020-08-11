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
package com.hivemq.cli.commands.hivemq.export;

import com.hivemq.cli.utils.LoggerUtils;
import com.opencsv.CSVWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.configuration.Configuration;
import picocli.CommandLine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractExportCommand {

    public enum OutputFormat {
        csv
    }

    @CommandLine.Option(names = {"-url"}, defaultValue = "http://localhost:8888", description = "The URL of the HiveMQ REST API endpoint (default http://localhost:8888)", order = 1)
    protected @NotNull String url;

    @CommandLine.Option(names = {"-f", "--file"}, description = "The file to write the output to (defaults to a timestamped file in the current working directory)", order = 2)
    protected @Nullable File file;

    @CommandLine.Option(names = {"-r", "--rate"}, defaultValue = "1500", description = "The rate limit of the rest calls to the HiveMQ API endpoint in requests per second (default 1500 rps)", order = 3)
    protected double rateLimit;

    @CommandLine.Option(names = {"--format"}, defaultValue = "csv", description = "The export output format (default csv)", order = 4)
    protected @NotNull OutputFormat format;

    @CommandLine.Option(names = {"--csvSeparator"}, defaultValue = "" + CSVWriter.DEFAULT_SEPARATOR, description = "The separator for CSV export (default " + CSVWriter.DEFAULT_SEPARATOR + ")", order = 5)
    public char csvSeparator;

    @CommandLine.Option(names = {"--csvQuoteChar"}, defaultValue = "" + CSVWriter.DEFAULT_QUOTE_CHARACTER, description = "The quote character for csv export (default " + CSVWriter.DEFAULT_QUOTE_CHARACTER + ")", order = 6)
    public char csvQuoteCharacter;

    @CommandLine.Option(names = {"--csvEscChar"}, defaultValue = "" + CSVWriter.DEFAULT_ESCAPE_CHARACTER, description = "The escape character for csv export (default " + CSVWriter.DEFAULT_ESCAPE_CHARACTER + ")", order = 7)
    public char csvEscapeChar;

    @CommandLine.Option(names = {"--csvLineEndChar"}, defaultValue = CSVWriter.DEFAULT_LINE_END, description = "The line-end character for csv export (default \\n)", order = 8)
    public @NotNull String csvLineEndCharacter;

    @CommandLine.Option(names = {"-l"}, defaultValue = "false", description = "Log to $HOME/.mqtt.cli/logs (Configurable through $HOME/.mqtt-cli/config.properties)", order = 9)
    private void initLogging(final boolean logToLogfile) {
        LoggerUtils.turnOffConsoleLogging(logToLogfile);
    }

    @Override
    public String toString() {
        return "AbstractExportCommand{" +
                "url='" + url + '\'' +
                ", file=" + file +
                ", rateLimit=" + rateLimit +
                ", format=" + format +
                ", csvSeparator=" + csvSeparator +
                ", csvQuoteCharacter=" + csvQuoteCharacter +
                ", csvEscapeChar=" + csvEscapeChar +
                ", csvLineEndCharacter='\\n" + '\'' +
                '}';
    }
}
