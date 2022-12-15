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

import com.hivemq.extension.sdk.api.packets.disconnect.DisconnectPacket;
import org.jetbrains.annotations.NotNull;

public class DisconnectInformation {

    private final @NotNull DisconnectPacket disconnectPacket;
    private final @NotNull String clientId;

    public DisconnectInformation(final @NotNull DisconnectPacket disconnectPacket, final @NotNull String clientId) {
        this.disconnectPacket = disconnectPacket;
        this.clientId = clientId;
    }

    public @NotNull DisconnectPacket getDisconnectPacket() {
        return disconnectPacket;
    }

    public @NotNull String getClientId() {
        return clientId;
    }
}
