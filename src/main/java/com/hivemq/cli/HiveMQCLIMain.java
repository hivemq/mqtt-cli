package com.hivemq.cli;

import com.hivemq.cli.ioc.DaggerHiveMQCLI;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.SizePolicy;
import org.pmw.tinylog.writers.ConsoleWriter;
import org.pmw.tinylog.writers.RollingFileWriter;
import picocli.CommandLine;

import java.security.Security;

public class HiveMQCLIMain {

    public static void main(String[] args) {

        Security.setProperty("crypto.policy", "unlimited");

        final CommandLine commandLine = DaggerHiveMQCLI.create().commandLine();

        Configurator.defaultConfig()
                .writer(new ConsoleWriter())
                .formatPattern("Client {context:identifier}: {message}")
                .level(Level.INFO)
                .activate();


        final int exitCode = commandLine.execute(args);

        System.exit(exitCode);

    }

}
