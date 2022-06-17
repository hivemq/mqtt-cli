package com.hivemq.cli.native_image.extensions

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class CliNativeExtensionImpl @Inject constructor(
    objectFactory: ObjectFactory,
)  : CliNativeExtension {

}
