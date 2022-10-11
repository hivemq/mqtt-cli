package com.hivemq.cli.utils.assertions;

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
