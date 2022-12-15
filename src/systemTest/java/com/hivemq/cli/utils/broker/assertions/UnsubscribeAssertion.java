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

package com.hivemq.cli.utils.broker.assertions;

import com.google.common.collect.ImmutableList;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.unsubscribe.UnsubscribePacket;
import com.hivemq.extensions.packets.general.UserPropertiesImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnsubscribeAssertion {

    private @NotNull List<String> topicFilters = List.of();
    private @NotNull UserProperties userProperties = UserPropertiesImpl.of(ImmutableList.of());

    private UnsubscribeAssertion() {
    }

    public static void assertUnsubscribePacket(
            final @NotNull UnsubscribePacket unsubscribePacket,
            final @NotNull Consumer<UnsubscribeAssertion> unsubscribeAssertionConsumer) {
        final UnsubscribeAssertion unsubscribeAssertion = new UnsubscribeAssertion();
        unsubscribeAssertionConsumer.accept(unsubscribeAssertion);

        assertEquals(unsubscribeAssertion.topicFilters, unsubscribePacket.getTopicFilters());
        assertEquals(unsubscribeAssertion.userProperties, unsubscribePacket.getUserProperties());
    }

    public void setTopicFilters(final @NotNull List<String> topicFilters) {
        this.topicFilters = topicFilters;
    }

    public void setUserProperties(final @NotNull UserProperties userProperties) {
        this.userProperties = userProperties;
    }
}
