package com.hivemq.cli.native_image.extensions

import org.gradle.api.provider.Property

interface CliNativeExtension {

    /**
     * Graal version, default: 22.2.0
     */
    val graalVersion: Property<String>

    /**
     * Java version, default: 17
     */
    val javaVersion: Property<String>

    /**
     * Graal download base url, default: https://github.com/graalvm/graalvm-ce-builds/releases/download
     */
    val graalBaseUrl: Property<String>
}