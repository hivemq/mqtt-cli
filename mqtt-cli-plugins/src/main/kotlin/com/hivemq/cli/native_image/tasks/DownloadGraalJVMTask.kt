package com.hivemq.cli.native_image.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.net.URL
import javax.inject.Inject

abstract class DownloadGraalJVMTask @Inject constructor(
    objectFactory: ObjectFactory
) : DefaultTask() {

    @get:Input
    val graalVersion = objectFactory.property<String>()

    @get:Input
    val javaVersion = objectFactory.property<String>()

    @get:Input
    val downloadBaseUrl = objectFactory.property<String>()

    @get:Internal
    val jdksDirectory: DirectoryProperty = objectFactory.directoryProperty().convention(
        project.layout.dir(
            project.providers.provider {
                project.gradle.gradleUserHomeDir.toPath()
                    .resolve("jdks")
                    //.resolve("com.hivemq.cli.native_image")
                    .toFile()
            }
        )
    )

    @get:Internal
    val graalFolderName = objectFactory.property<String>().convention(createGraalFolderName())

    @get:Internal
    val graalDownloadFileName = objectFactory.property<String>().convention(createDownloadGraalFileName())

    @get:OutputFile
    val graalDownload: Provider<RegularFile> = jdksDirectory.file(graalDownloadFileName)

    @TaskAction
    fun download() {
        URL(createDownloadUrl()).openStream().use { input ->
            graalDownload.get().asFile.outputStream().use { output -> input.copyTo(output) }
        }
    }

    private fun createDownloadGraalFileName(): Provider<String> {
        return createGraalFileName().map { graalFileName ->
            "${graalFileName}.${getArchiveExtension()}"
        }
    }

    private fun createGraalFileName(): Provider<String> {
        return javaVersion.zip(graalVersion) { javaVersion, graalVersion ->
            "graalvm-ce-java${javaVersion}-${getOperatingSystem()}-${getArchitecture()}-${graalVersion}"
        }
    }

    private fun createGraalFolderName(): Provider<String> {
        return javaVersion.zip(graalVersion) { javaVersion, graalVersion ->
            "graalvm-ce-java${javaVersion}-${graalVersion}"
        }
    }

    private fun createDownloadUrl(): String {
        return "${downloadBaseUrl.get()}/vm-${graalVersion.get()}/" + createDownloadGraalFileName().get()
    }

    private fun getOperatingSystem(): String {
        return if (DefaultNativePlatform.getCurrentOperatingSystem().isLinux) {
            "linux"
        } else if (DefaultNativePlatform.getCurrentOperatingSystem().isWindows) {
            "windows"
        } else if (DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
            "darwin"
        } else {
            throw IllegalStateException(
                "Unsupported operating system. (${DefaultNativePlatform.getCurrentOperatingSystem().displayName}"
            )
        }
    }

    private fun getArchitecture(): String {
        return if (DefaultNativePlatform.getCurrentArchitecture().isAmd64) {
            "amd64"
        } else if (DefaultNativePlatform.getCurrentArchitecture().isArm) {
            "aarch64"
        } else if (DefaultNativePlatform.getCurrentArchitecture().name == "arm-v8") { //used for M1 Apple devices
            "aarch64"
        } else {
            throw IllegalStateException("Unsupported system architecture. (${DefaultNativePlatform.getCurrentArchitecture().displayName})")
        }
    }

    private fun getArchiveExtension(): String {
        return when (getOperatingSystem()) {
            "windows" -> "zip"
            else -> "tar.gz"
        }
    }
}
