package com.hivemq.cli.native_image.extensions

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class CliNativeExtensionImpl @Inject constructor(
    objectFactory: ObjectFactory
) : CliNativeExtension {

    final override val javaVersion = objectFactory.property<String>().convention("21.0.2")
    final override val graalBaseUrl =
        objectFactory.property<String>().convention("https://github.com/graalvm/graalvm-ce-builds/releases/download")
}
