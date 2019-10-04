package com.hivemq.cli.utils;

import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.NotNull;


public class SubscribeMqttPublishCallbackUtils {

    public static @NotNull String applyBase64EncodingIfSet(final boolean encode, final byte[] payload) {
        if (encode) {
            return Base64.toBase64String(payload);
        } else {
            return new String(payload);
        }
    }
}
