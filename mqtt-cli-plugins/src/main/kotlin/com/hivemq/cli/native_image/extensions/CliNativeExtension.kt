package com.hivemq.cli.native_image.extensions

import org.gradle.api.provider.Property

interface CliNativeExtension {

    /**
     * Java version, default: 21.0.2
     */
    val javaVersion: Property<String>

    /**
     * Graal download base url, default: https://github.com/graalvm/graalvm-ce-builds/releases/download
     */
    val graalBaseUrl: Property<String>
}
