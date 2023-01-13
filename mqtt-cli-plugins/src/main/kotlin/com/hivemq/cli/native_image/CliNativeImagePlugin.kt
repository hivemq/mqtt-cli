package com.hivemq.cli.native_image

import com.hivemq.cli.native_image.extensions.CliNativeExtension
import com.hivemq.cli.native_image.extensions.CliNativeExtensionImpl
import com.hivemq.cli.native_image.tasks.DownloadGraalJVMTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform


class CliNativeImagePlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME: String = "cliNative"
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            CliNativeExtension::class, EXTENSION_NAME, CliNativeExtensionImpl::class
        )

        val downloadTask = project.tasks.register<DownloadGraalJVMTask>("downloadGraalJvm") {
            group = "native"
            description = "Configures the correct download for Graal"
            graalVersion.set(extension.graalVersion)
            javaVersion.set(extension.javaVersion)
            downloadBaseUrl.set(extension.graalBaseUrl)
        }

        val extractTask = project.tasks.register<Exec>("extractGraalJvm") {
            group = "native"
            description = "Unzips the Graal JVM into the auto provisioning JDKS folder"
            dependsOn(downloadTask)

            workingDir(downloadTask.flatMap { it.jdksDirectory })
            commandLine("tar", "-xzf", downloadTask.flatMap { it.graalDownload }.get())
            outputs.dir(downloadTask.flatMap { it.jdksDirectory.dir(it.graalFolderName) })
        }

        val extractTaskWindows = project.tasks.register<Copy>("extractGraalJvmWindows") {
            group = "native"
            description = "Unzips the Graal JVM into the auto provisioning JDKS folder"
            dependsOn(downloadTask)

            from(project.zipTree(downloadTask.flatMap { it.graalDownload }.get()))
            into(downloadTask.flatMap { it.jdksDirectory })
        }


        project.tasks.register<Exec>("installNativeImageTooling") {
            group = "native"
            description = "Installs the native-image tooling and declares the Graal as auto provisioned"

            if (DefaultNativePlatform.getCurrentOperatingSystem().isWindows) {
                dependsOn(extractTaskWindows)
                val graalDirectory = extractTaskWindows.map { it.destinationDir }
                    .zip(downloadTask.flatMap { it.graalFolderName }) { jdkFolder, graalFolder ->
                        jdkFolder.resolve(graalFolder)
                    }
                workingDir(graalDirectory)
                commandLine("cmd", "/C", getGuPath(), "install", "native-image")
                doLast {
                    graalDirectory.get().resolve("provisioned.ok").createNewFile()
                }
            } else {
                dependsOn(extractTask)
                workingDir(extractTask.map { it.outputs.files.singleFile })
                commandLine(getGuPath(), "install", "native-image")
                doLast {
                    extractTask.map { it.outputs.files.singleFile }.get().resolve("provisioned.ok").createNewFile()
                }
            }
        }
    }

    private fun getGuPath(): String {
        return if (DefaultNativePlatform.getCurrentOperatingSystem().isLinux) {
            "./bin/gu"
        } else if (DefaultNativePlatform.getCurrentOperatingSystem().isWindows) {
            "bin\\gu"
        } else if (DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
            "./Contents/Home/bin/gu"
        } else {
            throw IllegalStateException(
                "Unsupported operating system. (${DefaultNativePlatform.getCurrentOperatingSystem().displayName}"
            )
        }
    }
}
