package com.hivemq.cli.native_image.extensions

import org.gradle.api.provider.Property

interface CliNativeExtension {

    /**
     * GraalVM version, default: 25.2.4
     */
    val graalVersion: Property<String>

    /**
     * Graal download base url, default: https://github.com/graalvm/graalvm-ce-builds/releases/download
     */
    val graalBaseUrl: Property<String>
}
