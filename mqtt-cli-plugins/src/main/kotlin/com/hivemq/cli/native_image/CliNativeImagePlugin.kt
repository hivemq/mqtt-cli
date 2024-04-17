package com.hivemq.cli.native_image

import com.hivemq.cli.native_image.extensions.CliNativeExtension
import com.hivemq.cli.native_image.extensions.CliNativeExtensionImpl
import com.hivemq.cli.native_image.tasks.DownloadGraalJVMTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.io.File


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
            javaVersion.set(extension.javaVersion)
            downloadBaseUrl.set(extension.graalBaseUrl)
        }

        val extractTask = project.tasks.register<Exec>("extractGraalJvm") {
            group = "native"
            description = "Unzips the Graal JVM into the auto provisioning JDKS folder"

            inputs.file(downloadTask.flatMap { it.graalDownloadFile })
            outputs.dir(downloadTask.flatMap { it.jdksDirectory.dir(it.graalFolderName) })

            workingDir(downloadTask.flatMap { it.jdksDirectory.dir(it.graalFolderName) })
            commandLine("tar", "-xzf", downloadTask.flatMap { it.graalDownloadFile }.get(), "--strip-components=1")
        }

        val extractTaskWindows = project.tasks.register<Copy>("extractGraalJvmWindows") {
            group = "native"
            description = "Unzips the Graal JVM into the auto provisioning JDKS folder"

            inputs.file(downloadTask.flatMap { it.graalDownloadFile })

            from(project.zipTree(downloadTask.flatMap { it.graalDownloadFile }.get()))
            //rename("${downloadTask.map { it.graalFolderName }.get()}.*/(.+)", "$1")
            into(downloadTask.flatMap { it.jdksDirectory })
        }

        project.tasks.register("installNativeImageTooling") {
            group = "native"
            description = "Declares the GraalVM installation as auto provisioned"

            val graalVMFolderProvider: Provider<File>
            if (DefaultNativePlatform.getCurrentOperatingSystem().isWindows) {
                graalVMFolderProvider = extractTaskWindows.map { it.outputs.files.singleFile }
                inputs.dir(graalVMFolderProvider)
            } else {
                graalVMFolderProvider = extractTask.map { it.outputs.files.singleFile }
                inputs.dir(graalVMFolderProvider)
            }
            outputs.file(graalVMFolderProvider.get().resolve("provisioned.ok"))

            doLast {
                graalVMFolderProvider.get().resolve("provisioned.ok").createNewFile()
            }
        }
    }
}
