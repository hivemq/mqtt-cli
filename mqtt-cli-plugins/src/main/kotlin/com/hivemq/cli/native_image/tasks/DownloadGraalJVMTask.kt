package com.hivemq.cli.native_image.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.property
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.net.URL

abstract class DownloadGraalJVMTask : DefaultTask() {

    companion object {
        private const val GRAAL_VERSION_DEFAULT = "22.1.0"
        private val JAVA_VERSION_DEFAULT = JavaLanguageVersion.of("17")
        private const val BASE_URL_DEFAULT = "https://github.com/graalvm/graalvm-ce-builds/releases/download"
    }

    @get:Input
    val graalVersion = project.objects.property<String>().convention(GRAAL_VERSION_DEFAULT)

    @get:Input
    val javaVersion = project.objects.property<JavaLanguageVersion>().convention(JAVA_VERSION_DEFAULT)

    @get:Input
    val downloadBaseUrl = project.objects.property<String>().convention(BASE_URL_DEFAULT)

    @get:Internal
    val jdksDirectory: DirectoryProperty = project.objects.directoryProperty().convention(
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
    val graalFolderName = project.objects.property<String>().convention(createGraalFolderName())

    @get:Internal
    val graalDownloadFileName = project.objects.property<String>().convention(createDownloadGraalFileName())

    @get:OutputFile
    val graalDownload: Provider<RegularFile> = jdksDirectory.file(graalDownloadFileName)

    @TaskAction
    fun download() {
        URL(createDownloadUrl()).openStream().use { input ->
            graalDownload.get().asFile.outputStream().use { output -> input.copyTo(output) }
        }
    }

    private fun createDownloadGraalFileName(): String {
        return "${createGraalFileName()}.${getArchiveExtension()}"
    }

    private fun createGraalFileName(): String {
        return "graalvm-ce-java${javaVersion.get()}-${getOperatingSystem()}-${getArchitecture()}-${graalVersion.get()}"
    }

    private fun createGraalFolderName(): String {
        return "graalvm-ce-java${javaVersion.get()}-${graalVersion.get()}"
    }

    private fun createDownloadUrl(): String {
        return "${downloadBaseUrl.get()}/vm-${graalVersion.get()}/" + createDownloadGraalFileName()
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