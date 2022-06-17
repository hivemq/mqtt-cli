package com.hivemq.cli.native_image

import com.hivemq.cli.native_image.extensions.CliNativeExtension
import com.hivemq.cli.native_image.extensions.CliNativeExtensionImpl
import com.hivemq.cli.native_image.tasks.DownloadGraalJVMTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register


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
        }

        val extractTask = project.tasks.register<Exec>("extractGraalJvm") {
            group = "native"
            description = "Unzips the Graal JVM into the auto provisioning JDKS folder"
            dependsOn(downloadTask)

            workingDir(downloadTask.flatMap { it.jdksDirectory })
            commandLine("tar", "-xzf", downloadTask.flatMap { it.graalDownload }.get())
            outputs.dir(downloadTask.flatMap { it.jdksDirectory.dir(it.graalFolderName) })
        }

        val installTask = project.tasks.register<Exec>("installNativeImageTooling") {
            group = "native"
            description = "Installs the native-image tooling and declares the Graal as auto provisioned"
            dependsOn(extractTask)

            workingDir(extractTask.map { it.outputs.files.singleFile })
            commandLine("./Contents/Home/bin/gu", "install", "native-image")

            doLast {
                extractTask.map { it.outputs.files.singleFile }.get().resolve("provisioned.ok").createNewFile()
            }
        }
    }
}