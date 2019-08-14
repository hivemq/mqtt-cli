package com.hivemq.cli;

import com.hivemq.cli.ioc.DaggerHiveMQCLI;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.SizePolicy;
import org.pmw.tinylog.writers.RollingFileWriter;
import picocli.CommandLine;

import java.security.Security;

public class HiveMQCLIMain {

    public static void main(String[] args) {

        Security.setProperty("crypto.policy", "unlimited");

        Configurator.defaultConfig()
                .writer(new RollingFileWriter("hmq-mqtt-log.txt", 30, false, new TimestampLabeler("yyyy-MM-dd"), new SizePolicy(1024 * 10)))
                .formatPattern("{date:yyyy-MM-dd HH:mm:ss}: {{level}:|min-size=6} {context:identifier}: {message}")
                .level(Level.DEBUG)
                .activate();

        final CommandLine commandLine = DaggerHiveMQCLI.create().commandLine();

        final int exitCode = commandLine.execute(args);

        System.exit(exitCode);

    }

}
