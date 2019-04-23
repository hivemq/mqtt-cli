#!/usr/bin/env bash

## use graalvm in your paths..

native-image \
 -H:Name=mqtt \
 -H:+JNI \
 -H:+AllowVMInspection  \
 -H:+ReportExceptionStackTraces \
 -H:+ReportUnsupportedElementsAtRuntime \
 -H:ReflectionConfigurationFiles=reflection.json \
 --report-unsupported-elements-at-runtime \
 --allow-incomplete-classpath \
 --enable-all-security-services \
 --no-server com.hivemq.cli.Mqtt  \
 --delay-class-initialization-to-runtime=org.jctools.util.UnsafeRefArrayAccess,com.hivemq.client.internal.mqtt.codec.encoder.mqtt3.Mqtt3DisconnectEncoder,com.hivemq.client.internal.mqtt.codec.encoder.MqttPingReqEncoder,io.netty.handler.codec.http.HttpObjectEncoder,io.netty.channel.unix.FileDescriptor,io.netty.channel.epoll.Native,io.netty.channel.unix.Errors,io.netty.channel.epoll.EpollEventLoop,io.netty.channel.epoll.EpollEventArray,io.netty.channel.unix.IovArray,io.netty.channel.unix.Limits,io.netty.handler.ssl.ReferenceCountedOpenSslEngine,io.netty.handler.ssl.ConscryptAlpnSslEngine,io.netty.handler.ssl.ReferenceCountedOpenSslServerContext,io.netty.handler.ssl.ReferenceCountedOpenSslClientContext,io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator,io.netty.handler.ssl.JettyNpnSslEngine \
 --rerun-class-initialization-at-runtime='sun.security.jca.JCAUtil$CachedSecureRandomHolder,javax.net.ssl.SSLContext,io.netty.buffer.UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf,io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder' \
 -cp target/mqtt-cli-1.0-SNAPSHOT.jar


# --delay-class-initialization-to-runtime=io.netty.util.internal.logging.Log4JLogger,io.netty.handler.ssl.JettyNpnSslEngine,com.hivemq.client.internal.mqtt.codec.encoder.mqtt3.Mqtt3DisconnectEncoder,com.hivemq.client.internal.mqtt.codec.encoder.MqttPingReqEncoder,io.netty.channel.unix.FileDescriptor,io.netty.channel.epoll.Native,io.netty.channel.unix.Errors,io.netty.channel.epoll.EpollEventLoop,io.netty.channel.unix.Limits,io.netty.channel.epoll.EpollEventArray,io.netty.channel.unix.IovArray,io.netty.handler.ssl.ReferenceCountedOpenSslEngine,io.netty.handler.ssl.ConscryptAlpnSslEngine,io.netty.handler.ssl.ReferenceCountedOpenSslClientContext,io.netty.handler.ssl.ReferenceCountedOpenSslServerContext,io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator,io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder,com.sun.jndi.dns.DnsClient \
# --rerun-class-initialization-at-runtime='io.netty.handler.ssl.util.ThreadLocalInsecureRandom,io.netty.buffer.UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf,sun.security.jca.JCAUtil$CachedSecureRandomHolder,javax.net.ssl.SSLContext' \
#-Dio.netty.noUnsafe=true \