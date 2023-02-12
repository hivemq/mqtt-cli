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

import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.UnsubscribeOptions;
import com.hivemq.cli.mqtt.clients.CliMqttClient;
import com.hivemq.cli.mqtt.clients.ShellClients;
import com.hivemq.cli.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CommandLine.Command(name = "sub",
                     aliases = "subscribe",
                     description = "Subscribe this MQTT client to a list of topics",
                     mixinStandardHelpOptions = true)
public class ContextSubscribeCommand implements Callable<Integer> {

    private static final int IDLE_TIME = 1000;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-s", "--stay"},
                        defaultValue = "false",
                        description = "The subscribe will block the console and wait for publish messages to print (default: false)")
    private boolean stay;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-oc", "--outputToConsole"},
                        defaultValue = "false",
                        description = "The received messages will be written to the console (default: true)")
    private void printToSTDOUT(final boolean printToSTDOUT) {
        subscribeOptions.setPrintToSTDOUT(printToSTDOUT);
    }

    @CommandLine.Mixin
    private final @NotNull SubscribeOptions subscribeOptions = new SubscribeOptions();

    private final @NotNull ShellClients shellClients;

    @Inject
    public ContextSubscribeCommand(final @NotNull ShellClients shellClients) {
        this.shellClients = shellClients;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {} ", this);

        final CliMqttClient client = shellClients.getContextClient();
        if (client == null) {
            Logger.error("The client to subscribe with does not exist");
            return 1;
        }

        subscribeOptions.setDefaultOptions();
        subscribeOptions.logUnusedOptions(client.getMqttVersion());
        subscribeOptions.arrangeQosToMatchTopics();

        if (stay) {
            subscribeOptions.setPrintToSTDOUT(true);
        }

        if (subscribeOptions.isOutputFileInvalid(subscribeOptions.getOutputFile())) {
            return 1;
        }

        try {
            client.subscribe(subscribeOptions);
        } catch (final Exception ex) {
            LoggerUtils.logShellError("Unable to subscribe", ex);
            return 1;
        }

        if (stay) {
            try {
                stay(client);
            } catch (final InterruptedException ex) {
                LoggerUtils.logShellError("Unable to stay", ex);
                return 1;
            }
        }

        return 0;
    }

    private void stay(final @NotNull CliMqttClient client) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        final Runnable waitForDisconnectRunnable = () -> {
            while (client.isConnected()) {
                try {
                    Thread.sleep(IDLE_TIME);
                } catch (final InterruptedException e) {
                    return;
                }
            }
            latch.countDown();
        };

        final Runnable waitForExitCommandRunnable = () -> {
            final Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
            latch.countDown();
        };

        final ExecutorService WORKER_THREADS = Executors.newFixedThreadPool(2);

        WORKER_THREADS.submit(waitForDisconnectRunnable);
        WORKER_THREADS.submit(waitForExitCommandRunnable);

        latch.await();

        WORKER_THREADS.shutdownNow();

        if (!client.isConnected()) {
            shellClients.removeContextClient();
        } else {
            client.unsubscribe(UnsubscribeOptions.of(subscribeOptions));
        }
    }

    @Override
    public @NotNull String toString() {
        return "ContextSubscribeCommand{" + "stay=" + stay + ", subscribeOptions=" + subscribeOptions + '}';
    }
}
