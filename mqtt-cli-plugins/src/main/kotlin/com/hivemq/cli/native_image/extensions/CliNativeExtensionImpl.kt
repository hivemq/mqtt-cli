package com.hivemq.cli.native_image.extensions

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class CliNativeExtensionImpl @Inject constructor(
    objectFactory: ObjectFactory
) : CliNativeExtension {

    final override val graalVersion = objectFactory.property<String>().convention("22.2.0")
    final override val javaVersion = objectFactory.property<String>().convention("17")
    final override val graalBaseUrl =
        objectFactory.property<String>().convention("https://github.com/graalvm/graalvm-ce-builds/releases/download")
}
