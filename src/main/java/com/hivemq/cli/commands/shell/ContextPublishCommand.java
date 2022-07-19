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

import com.google.common.base.Throwables;
import com.hivemq.cli.commands.options.PublishOptions;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "pub", aliases = "publish", description = "Publish a message to a list of topics")
public class ContextPublishCommand extends ShellContextCommand implements Callable<Integer> {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Mixin
    private final @NotNull PublishOptions publishOptions = new PublishOptions();

    @SuppressWarnings("unused") //needed for pico cli - reflection code generation
    public ContextPublishCommand() {
        //noinspection ConstantConditions
        this(null);
    }

    @Inject
    public ContextPublishCommand(final @NotNull MqttClientExecutor executor) {
        super(executor);
    }

    @Override
    public Integer call() {

        Logger.trace("Command {} ", this);

        if (contextClient != null) {
            publishOptions.logUnusedOptions(contextClient.getConfig().getMqttVersion());

            try {
                publishOptions.arrangeQosToMatchTopics();
                mqttClientExecutor.publish(contextClient, publishOptions);
            } catch (final Exception ex) {
                Logger.error(ex, "Unable to publish: {}", Throwables.getRootCause(ex).getMessage());
                return 1;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        return "ContextPublishCommand{" + "publishOptions=" +
                publishOptions + '}';
    }

}
