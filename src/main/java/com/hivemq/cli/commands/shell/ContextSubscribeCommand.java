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
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.UnsubscribeOptions;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CommandLine.Command(name = "sub", aliases = "subscribe",
        description = "Subscribe this MQTT client to a list of topics")
public class ContextSubscribeCommand extends ShellContextCommand implements Callable<Integer> {

    private static final int IDLE_TIME = 1000;

    @CommandLine.Mixin
    private final @NotNull SubscribeOptions subscribeOptions = new SubscribeOptions();

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-s", "--stay"}, defaultValue = "false",
            description = "The subscribe will block the console and wait for publish messages to print (default: false)")
    private boolean stay;

    @SuppressWarnings("unused") //needed for pico cli - reflection code generation
    public ContextSubscribeCommand() {
        //noinspection ConstantConditions
        this(null);
    }

    @Inject
    public ContextSubscribeCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }

    @Override
    public Integer call() {

        Logger.trace("Command {} ", this);

        if (contextClient == null) {
            Logger.error("The client to subscribe with does not exist");
            return 1;
        }

        subscribeOptions.setDefaultOptions();
        subscribeOptions.logUnusedOptions(contextClient.getConfig().getMqttVersion());

        if (!stay) {
            subscribeOptions.setPrintToSTDOUT(false);
        }

        if (subscribeOptions.isOutputFileInvalid(subscribeOptions.getOutputFile())) {
            return 1;
        }

        try {
            subscribeOptions.arrangeQosToMatchTopics();
            mqttClientExecutor.subscribe(Objects.requireNonNull(contextClient), subscribeOptions);
        } catch (final Exception ex) {
            Logger.error(ex, "Unable to subscribe: {}", Throwables.getRootCause(ex).getMessage());
        }

        if (stay) {
            try {
                stay();
            } catch (final InterruptedException ex) {
                Logger.error(ex, "Interrupted while staying after subscribe: {}",Throwables.getRootCause(ex).getMessage());
                return 1;
            }
        }

        return 0;
    }

    private void stay() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        final Runnable waitForDisconnectRunnable = () -> {
            while (Objects.requireNonNull(contextClient).getState().isConnected()) {
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

        if (contextClient != null) {
            if (!contextClient.getState().isConnectedOrReconnect()) {
                removeContext();
            } else {
                mqttClientExecutor.unsubscribe(contextClient, UnsubscribeOptions.of(subscribeOptions));
            }
        }
    }

    @Override
    public String toString() {
        return "ContextSubscribeCommand{" + "subscribeOptions=" + subscribeOptions + ", usageHelpRequested=" +
                usageHelpRequested + ", stay=" + stay + '}';
    }
}
