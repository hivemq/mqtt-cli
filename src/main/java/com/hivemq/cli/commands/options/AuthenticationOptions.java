package com.hivemq.cli.commands.options;

import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.converters.ByteBufferConverter;
import com.hivemq.cli.converters.EnvVarToByteBufferConverter;
import com.hivemq.cli.converters.FileToByteBufferConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.nio.ByteBuffer;

public class AuthenticationOptions {

    @CommandLine.Option(names = {"-u", "--user"}, description = "The username for authentication", order = 2)
    @Nullable
    private String user;

    @CommandLine.Option(names = {"-pw", "--password"}, arity = "0..1", interactive = true, converter = ByteBufferConverter.class, description = "The password for authentication", order = 2)
    @Nullable
    private ByteBuffer password;

    @CommandLine.Option(names = {"-pw:env"}, arity = "0..1", converter = EnvVarToByteBufferConverter.class, fallbackValue = "MQTT_CLI_PW", description = "The password for authentication read in from an environment variable", order = 2)
    private void setPasswordFromEnv(final @NotNull ByteBuffer passwordEnvironmentVariable) { password = passwordEnvironmentVariable; }

    @CommandLine.Option(names = {"-pw:file"}, converter = FileToByteBufferConverter.class, description = "The password for authentication read in from a file", order = 2)
    private void setPasswordFromFile(final @NotNull ByteBuffer passwordFromFile) {
        password = passwordFromFile;
    }

    public AuthenticationOptions() {
        setDefaultOptions();
    }

    private void setDefaultOptions() {
        final DefaultCLIProperties properties = MqttCLIMain.MQTTCLI.defaultCLIProperties();
        if (user == null) { user = properties.getUsername(); }
        if (password == null) {
            try {
                password = properties.getPassword();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //TODO Log error instead of throwing the exception
        }
    }

    public @Nullable String getUser() { return user; }

    public @Nullable ByteBuffer getPassword() { return password; }
}
