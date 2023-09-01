/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.options.UnsubscribeOptions;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "unsub",
                     aliases = "unsubscribe",
                     description = "Unsubscribe this MQTT client from a list of topics",
                     mixinStandardHelpOptions = true)
public class ContextUnsubscribeCommand extends ShellContextCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private final @NotNull UnsubscribeOptions unsubscribeOptions = new UnsubscribeOptions();

    @Inject
    public ContextUnsubscribeCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        if (contextClient == null) {
            Logger.error("The client to unsubscribe with does not exist");
            return 1;
        }

        unsubscribeOptions.logUnusedUnsubscribeOptions(contextClient.getConfig().getMqttVersion());

        try {
            mqttClientExecutor.unsubscribe(contextClient, unsubscribeOptions);
        } catch (final Exception ex) {
            LoggerUtils.logShellError("Unable to unsubscribe", ex);
            return 1;
        }

        return 0;
    }

    @Override
    public @NotNull String toString() {
        return "ContextUnsubscribeCommand{" + "unsubscribeOptions=" + unsubscribeOptions + '}';
    }
}
