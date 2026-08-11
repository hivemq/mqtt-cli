package com.hivemq.cli.native_image.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
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

abstract class DownloadGraalJVMTask @Inject constructor(objectFactory: ObjectFactory) : DefaultTask() {

    @get:Input
    val graalVersion = objectFactory.property<String>()

    @get:Input
    val downloadBaseUrl = objectFactory.property<String>()

    @get:Internal
    val jdksDirectory: DirectoryProperty =
        objectFactory.directoryProperty().convention(project.layout.dir(project.providers.provider {
            project.gradle.gradleUserHomeDir.toPath().resolve("jdks").toFile()
        }))

    @get:Internal
    val graalFolderName = objectFactory.property<String>().convention(createGraalFolderName())

    @get:Internal
    val graalDownloadFileName = objectFactory.property<String>().convention(createGraalFileName())

    @get:OutputFile
    protected val graalDownloadFileProperty: RegularFileProperty =
        project.objects.fileProperty().value(jdksDirectory.file(graalDownloadFileName))

    @get:Internal
    val graalDownloadFile: Provider<RegularFile> = graalDownloadFileProperty


    @TaskAction
    fun download() {
        URL(createDownloadUrl()).openStream().use { input ->
            graalDownloadFile.get().asFile.outputStream().use { output -> input.copyTo(output) }
        }
    }

    private fun createGraalFolderName(): Provider<String> {
        return graalVersion.map { graalVersion ->
            "graalvm-community-openjdk-${graalVersion}"
        }
    }

    private fun createGraalFileName(): Provider<String> {
        return graalVersion.map { graalVersion ->
            "graalvm-community-jdk-${createJdkIdentifier(graalVersion)}_${getOperatingSystem()}-${getArchitecture()}_bin.${getArchiveExtension()}"
        }
    }

    // release assets encode the feature release train and the JDK base, GraalVM 25.2.4 ships as 25i2-25.0.4
    private fun createJdkIdentifier(graalVersion: String): String {
        val parts = graalVersion.split(".")
        require(parts.size == 3) {
            "Expected a GraalVM version of the form <major>.<minor>.<security>, but was '${graalVersion}'."
        }
        return "${parts[0]}i${parts[1]}-${parts[0]}.0.${parts[2]}"
    }

    private fun createDownloadUrl() =
        "${downloadBaseUrl.get()}/graal-${graalVersion.get()}/${createGraalFileName().get()}"

    private fun getOperatingSystem(): String {
        return if (DefaultNativePlatform.getCurrentOperatingSystem().isLinux) {
            "linux"
        } else if (DefaultNativePlatform.getCurrentOperatingSystem().isWindows) {
            "windows"
        } else if (DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
            "macos"
        } else {
            throw IllegalStateException(
                "Unsupported operating system. (${DefaultNativePlatform.getCurrentOperatingSystem().displayName}"
            )
        }
    }

    private fun getArchitecture(): String {
        return if (DefaultNativePlatform.getCurrentArchitecture().isAmd64) {
            "x64"
        } else if (DefaultNativePlatform.getCurrentArchitecture().isArm) {
            "aarch64"
        } else if (DefaultNativePlatform.getCurrentArchitecture().name == "arm-v8") { // used for M1 Apple devices
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
