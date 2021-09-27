import com.netflix.gradle.plugins.packaging.CopySpecEnhancement
import nl.javadude.gradle.plugins.license.DownloadLicensesExtension.license
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.redline_rpm.header.Flags
import org.redline_rpm.header.Os
import org.redline_rpm.header.RpmType
import org.redline_rpm.payload.Directive
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

buildscript {
    dependencies {
        if (gradle.includedBuilds.find { it.name == "plugins" } != null) {
            classpath("com.hivemq:plugins")
        }
    }
}

plugins {
    java
    application
    id("com.github.johnrengelman.shadow")
    id("com.github.hierynomus.license")
    id("nebula.ospackage")
    id("edu.sc.seis.launch4j")
    id("com.palantir.graal")
    id("de.thetaphi.forbiddenapis")
    id("com.github.breadmoirai.github-release")
    id("org.ajoberstar.git-publish")
    id("org.owasp.dependencycheck")
    id("com.github.ben-manes.versions")
    id("org.openapi.generator")
    id("com.google.cloud.tools.jib")
}

/* ******************** metadata ******************** */

group = "com.hivemq"
description = "MQTT CLI is a tool that provides a feature rich command line interface for connecting, " +
        "publishing, subscribing, unsubscribing and disconnecting " +
        "various MQTT clients simultaneously and supports  MQTT 5.0 and MQTT 3.1.1 "

application {
    @Suppress("DEPRECATION") // ShadowJar needs deprecated mainClassName
    mainClassName = "com.hivemq.cli.MqttCLIMain"
}

/* ******************** java ******************** */

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.jar {
    manifest.attributes(
        "Built-JDK" to System.getProperty("java.version"),
        "Implementation-Title" to "MQTT CLI",
        "Implementation-Version" to project.version,
        "Implementation-Vendor" to "HiveMQ GmbH",
        "Specification-Title" to "MQTT CLI",
        "Specification-Version" to project.version,
        "Specification-Vendor" to "HiveMQ GmbH",
        "Main-Class" to application.mainClass.get(),
        "Built-Date" to SimpleDateFormat("yyyy-MM-dd").format(Date())
    )
}

tasks.shadowJar {
    archiveClassifier.set("")
}

/* ******************** dependencies ******************** */

repositories {
    mavenCentral()
}

dependencies {

    implementation("io.swagger:swagger-annotations:${property("swagger.version")}")
    implementation("com.google.code.findbugs:jsr305:${property("findBugs.version")}")
    implementation("com.squareup.okhttp3:okhttp:${property("okHttp.version")}")
    implementation("com.squareup.okhttp3:logging-interceptor:${property("okHttp.version")}")
    implementation("io.gsonfire:gson-fire:${property("gsonFire.version")}")
    implementation("org.apache.commons:commons-lang3:${property("commonsLang.version")}")
    implementation("javax.annotation:javax.annotation-api:${property("javax.version")}")

    implementation("org.jline:jline:${property("jline.version")}")
    implementation("org.jline:jline-terminal-jansi:${property("jline.version")}")
    implementation("com.google.dagger:dagger:${property("dagger.version")}")
    compileOnly("org.graalvm.nativeimage:svm:${property("substrateVm.version")}")
    annotationProcessor("com.google.dagger:dagger-compiler:${property("dagger.version")}")

    implementation("info.picocli:picocli:${property("picocli.version")}")
    implementation("info.picocli:picocli-shell-jline3:${property("picoclishell.version")}")
    implementation("info.picocli:picocli-codegen:${property("picocli.version")}")
    implementation("com.google.guava:guava:${property("guava.version")}")
    implementation("com.google.code.gson:gson:${property("gson.version")}")
    implementation("org.tinylog:tinylog-api:${property("tinylog.version")}")
    implementation("org.tinylog:tinylog-impl:${property("tinylog.version")}")
    implementation("org.jetbrains:annotations:${property("jetbrainsAnnotations.version")}")
    implementation("org.bouncycastle:bcprov-jdk15on:${property("bouncycastle.version")}")
    implementation("org.bouncycastle:bcpkix-jdk15on:${property("bouncycastle.version")}")
    implementation("com.hivemq:hivemq-mqtt-client:${property("hivemqclient.version")}")
    implementation("io.netty:netty-handler:${property("netty.version")}")
    implementation("io.netty:netty-codec-http:${property("netty.version")}")
    implementation("io.netty:netty-transport-native-epoll:${property("netty.version")}:linux-x86_64")
    implementation("com.opencsv:opencsv:${property("openCsv.version")}")

    testImplementation("org.mockito:mockito-core:${property("mockito.version")}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junitJupiter.version")}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${property("junitJupiter.version")}")
    testImplementation("com.squareup.okhttp3:mockwebserver:${property("okHttp.version")}")
    testImplementation("com.hivemq:hivemq-testcontainer-junit5:${property("hivemqTestcontainer.version")}")
    testImplementation("com.ginsberg:junit5-system-exit:${property("systemExit.version")}")
    testImplementation("org.testcontainers:testcontainers:${property("testcontainers.version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junitJupiter.version")}")

}

/* ******************** OpenAPI ******************** */

val generateHivemqOpenApi by tasks.registering(GenerateTask::class) {
    group = "hivemq"
    generatorName.set("java")
    inputSpec.set("$projectDir/specs/HiveMQ-${project.property("hivemq-api.version")}-OpenAPI-spec.yaml")
    outputDir.set("$buildDir/tmp/$name")
    apiPackage.set("com.hivemq.cli.openapi.hivemq")
    modelPackage.set("com.hivemq.cli.openapi.hivemq")
    configOptions.put("dateLibrary", "java8")
    configOptions.put("hideGenerationTimestamp", "true")

    inputs.file(inputSpec.get()).withPropertyName("inputSpec").withPathSensitivity(PathSensitivity.NONE)
    val outputSrcDir = buildDir.resolve("generated/openapi/hivemq/java")
    outputs.dir(outputSrcDir).withPropertyName("outputSrcDir")
    outputs.cacheIf { true }
    doFirst { delete(outputDir) }
    doLast {
        copy {
            from("${outputDir.get()}/src/main/java")
            into(outputSrcDir)
        }
    }
}

sourceSets.main {
    java {
        srcDir(generateHivemqOpenApi)
    }
}

/* ******************** test ******************** */

tasks.test {
    useJUnitPlatform()
}

/* ******************** compliance ******************** */

license {
    header = projectDir.resolve("HEADER")
    include("**/*.java")
    exclude("**/com/hivemq/cli/openapi/**")
    mapping("java", "SLASHSTAR_STYLE")
}

downloadLicenses {
    aliases = mapOf(
        license("Apache License, Version 2.0", "https://opensource.org/licenses/Apache-2.0") to listOf(
            "Apache 2",
            "Apache 2.0",
            "Apache License 2.0",
            "Apache License, 2.0",
            "Apache License v2.0",
            "Apache License, Version 2",
            "Apache License Version 2.0",
            "Apache License, Version 2.0",
            "Apache License, version 2.0",
            "The Apache License, Version 2.0",
            "Apache Software License - Version 2.0",
            "Apache Software License, version 2.0",
            "The Apache Software License, Version 2.0"
        ),
        license("MIT License", "https://opensource.org/licenses/MIT") to listOf(
            "MIT License",
            "MIT license",
            "The MIT License",
            "The MIT License (MIT)"
        ),
        license("CDDL, Version 1.0", "https://opensource.org/licenses/CDDL-1.0") to listOf(
            "CDDL, Version 1.0",
            "Common Development and Distribution License 1.0",
            "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0",
            license("CDDL", "https://glassfish.dev.java.net/public/CDDLv1.0.html")
        ),
        license("CDDL, Version 1.1", "https://oss.oracle.com/licenses/CDDL+GPL-1.1") to listOf(
            "CDDL 1.1",
            "CDDL, Version 1.1",
            "Common Development And Distribution License 1.1",
            "CDDL+GPL License",
            "CDDL + GPLv2 with classpath exception",
            "Dual license consisting of the CDDL v1.1 and GPL v2",
            "CDDL or GPLv2 with exceptions",
            "CDDL/GPLv2+CE"
        ),
        license("LGPL, Version 2.0", "https://opensource.org/licenses/LGPL-2.0") to listOf(
            "LGPL, Version 2.0",
            "GNU General Public License, version 2"
        ),
        license("LGPL, Version 2.1", "https://opensource.org/licenses/LGPL-2.1") to listOf(
            "LGPL, Version 2.1",
            "LGPL, version 2.1",
            "GNU Lesser General Public License version 2.1 (LGPLv2.1)",
            license("GNU Lesser General Public License", "http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html")
        ),
        license("LGPL, Version 3.0", "https://opensource.org/licenses/LGPL-3.0") to listOf(
            "LGPL, Version 3.0",
            "Lesser General Public License, version 3 or greater"
        ),
        license("EPL, Version 1.0", "https://opensource.org/licenses/EPL-1.0") to listOf(
            "EPL, Version 1.0",
            "Eclipse Public License - v 1.0",
            "Eclipse Public License - Version 1.0",
            license("Eclipse Public License", "http://www.eclipse.org/legal/epl-v10.html")
        ),
        license("EPL, Version 2.0", "https://opensource.org/licenses/EPL-2.0") to listOf(
            "EPL 2.0",
            "EPL, Version 2.0"
        ),
        license("EDL, Version 1.0", "https://www.eclipse.org/org/documents/edl-v10.php") to listOf(
            "EDL 1.0",
            "EDL, Version 1.0",
            "Eclipse Distribution License - v 1.0"
        ),
        license("BSD 3-Clause License", "https://opensource.org/licenses/BSD-3-Clause") to listOf(
            "BSD 3-clause",
            "BSD-3-Clause",
            "BSD 3-Clause License",
            "3-Clause BSD License",
            "New BSD License",
            license("BSD", "http://asm.ow2.org/license.html"),
            license("BSD", "http://asm.objectweb.org/license.html"),
            license("BSD", "LICENSE.txt")
        ),
        license("Bouncy Castle License", "https://www.bouncycastle.org/licence.html") to listOf(
            "Bouncy Castle Licence"
        ),
        license("W3C License", "https://opensource.org/licenses/W3C") to listOf(
            "W3C License",
            "W3C Software Copyright Notice and License",
            "The W3C Software License"
        ),
        license("CC0", "https://creativecommons.org/publicdomain/zero/1.0/") to listOf(
            "CC0",
            "Public Domain"
        )
    )

    dependencyConfiguration = "runtimeClasspath"
}

val updateThirdPartyLicenses by tasks.registering {
    group = "license"
    dependsOn(tasks.downloadLicenses)
    doLast {
        javaexec {
            main = "-jar"
            args(
                "$projectDir/gradle/tools/license-third-party-tool-3.0.jar",
                "The MQTT Cli",
                "$buildDir/reports/license/dependency-license.xml",
                "$projectDir/src/distribution/third-party-licenses/licenses",
                "$projectDir/src/distribution/third-party-licenses/licenses.html"
            )
        }
    }
}

forbiddenApis {
    bundledSignatures = setOf("jdk-deprecated", "jdk-non-portable", "jdk-reflection")
}

tasks.forbiddenApisMain {
    exclude("**/LoggingBootstrap.class")
}

tasks.forbiddenApisTest { enabled = false }

/* ******************** graal ******************** */

graal {
    graalVersion("19.2.1")
    outputName(project.name)
    mainClass(application.mainClass.get())
    option("-H:+PrintClassInitialization")
    option("-H:ReflectionConfigurationFiles=tools/reflection.json")
    option("-H:-UseServiceLoaderFeature")
    option("-H:IncludeResources=\"org/jline/utils/*.*")
    option("-H:IncludeResources=\"org/jline/terminal/*.*")
    option("--allow-incomplete-classpath")
    option("--report-unsupported-elements-at-runtime")
    option("--initialize-at-build-time")
    option(
        "--initialize-at-run-time=" +
                "io.netty.channel.unix.Errors," +
                "io.netty.channel.unix.IovArray," +
                "io.netty.channel.unix.Limits," +
                "io.netty.channel.unix.Socket," +
                "io.netty.channel.epoll.EpollEventArray," +
                "io.netty.channel.epoll.EpollEventLoop," +
                "io.netty.channel.epoll.Native," +
                "io.netty.handler.ssl.ConscryptAlpnSslEngine," +
                "io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator," +
                "io.netty.handler.ssl.JettyNpnSslEngine," +
                "io.netty.handler.ssl.ReferenceCountedOpenSslEngine," +
                "io.netty.handler.ssl.ReferenceCountedOpenSslContext," +
                "io.netty.handler.codec.http.HttpObjectEncoder," +
                "io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder," +
                "com.hivemq.client.internal.mqtt.codec.encoder.MqttPingReqEncoder," +
                "com.hivemq.client.internal.mqtt.codec.encoder.mqtt3.Mqtt3DisconnectEncoder"
    )
}

/* ******************** homebrew package & formula ******************** */

val buildBrewZip by tasks.registering(Zip::class) {

    archiveClassifier.set("brew")
    destinationDirectory.set(buildDir.resolve("packages/homebrew"))

    into("brew") {
        from(tasks.shadowJar)
        from(projectDir.resolve("packages/homebrew/mqtt"))
    }
    from(projectDir.resolve("LICENSE")) {
        into("licenses")
    }
}

val buildBrewFormula by tasks.registering(Copy::class) {
    dependsOn(buildBrewZip)

    from(projectDir.resolve("packages/homebrew/mqtt-cli.rb"))
    into(buildDir.resolve("packages/homebrew"))
    filter {
        it.replace("@@description@@", project.description!!)
            .replace("@@version@@", project.version.toString())
            .replace("@@filename@@", buildBrewZip.get().archiveFileName.get())
            .replace("@@shasum@@", sha256Hash(buildBrewZip.get().archiveFile.get().asFile))
    }
}

/* ******************** debian and rpm packages ******************** */

ospackage {
    packageName = project.name
    version = project.version.toString()

    url = "https://www.hivemq.com/"

    summary = "MQTT Command Line Interface for interacting with a MQTT broker"
    license = "apache2"
    packager = ""
    vendor = "HiveMQ GmbH"

    os = Os.LINUX
    type = RpmType.BINARY

    user = "root"
    permissionGroup = "root"

    into("/opt/$packageName")
    from(tasks.shadowJar)
    from(projectDir.resolve("packages/linux/mqtt"), closureOf<CopySpec> {
        fileMode = 0b111_101_101 // 0755
        filter {
            it.replace("@@jarPath@@", "/opt/$packageName/${tasks.shadowJar.get().archiveFileName.get()}")
        }
    })
    from(projectDir.resolve("LICENSE"), closureOf<CopySpec> {
        into("licenses")
        CopySpecEnhancement.fileType(this, Directive.LICENSE)
    })

    link("/usr/bin/mqtt", "/opt/$packageName/mqtt", 0b111_101_101)
}

tasks.buildDeb {
    requires("default-jre").or("java8-runtime")
}

tasks.buildRpm {
    release = "1"
    requires("jre", "1.8.0", Flags.GREATER or Flags.EQUAL)
}

val buildDebianPackage by tasks.registering(Copy::class) {
    from(tasks.buildDeb.flatMap { it.archiveFile })
    into(buildDir.resolve("packages/debian"))
    rename { "${project.name}-${project.version}.deb" }
}

val buildRpmPackage by tasks.registering(Copy::class) {
    from(tasks.buildRpm.flatMap { it.archiveFile })
    into(buildDir.resolve("packages/rpm"))
    rename { "${project.name}-${project.version}.rpm" }
}

/* ******************** windows zip ******************** */

launch4j {
    headerType = "console"
    mainClassName = application.mainClass.get()
    icon = "$projectDir/icons/05-mqtt-cli-icon.ico"
    jarTask = tasks.shadowJar.get()
    copyConfigurable = emptyList<Any>()
    copyright = "Copyright 2019-present HiveMQ and the HiveMQ Community"
    companyName = "HiveMQ GmbH"
    downloadUrl = "https://openjdk.java.net/install/"
    jreMinVersion = "1.8"
    windowTitle = "MQTT CLI"
    version = project.version.toString()
    textVersion = project.version.toString()
}

val buildWindowsZip by tasks.registering(Zip::class) {

    archiveClassifier.set("win")
    destinationDirectory.set(buildDir.resolve("packages/windows"))

    from(projectDir.resolve("packages/windows")) {
        filter { it.replace("@@exeName@@", launch4j.outfile) }
    }
    from(tasks.createExe.map { it.dest })
    from("$projectDir/LICENSE")
}

/* ******************** packages ******************** */

val buildPackages by tasks.registering {
    dependsOn(buildBrewFormula, buildDebianPackage, buildRpmPackage, buildWindowsZip)
}

/* ******************** Publish Draft-Release with all packages to GitHub Releases ******************** */

githubRelease {
    token(System.getenv("githubToken"))
    draft.set(true)
    releaseAssets(
        tasks.shadowJar,
        buildBrewZip,
        buildDebianPackage.map { fileTree(it.destinationDir) },
        buildRpmPackage.map { fileTree(it.destinationDir) },
        buildWindowsZip
    )
    allowUploadToExisting.set(true)
}

/* ******************** Update the Homebrew-Formula with the newly built package ******************** */

gitPublish {
    repoUri.set("https://github.com/hivemq/homebrew-mqtt-cli.git")
    branch.set("master")
    commitMessage.set("Release version v${project.version}")
    contents.from(buildBrewFormula) { include("mqtt-cli.rb") }
}

/* ******************** docker ******************** */

jib {
    from {
        image = "openjdk:11-jre-slim-buster"
    }
    to {
        image = "hivemq/mqtt-cli"
        tags = setOf(project.version.toString())
        auth {
            username = System.getenv("DOCKER_USER") ?: ""
            password = System.getenv("DOCKER_PASSWORD") ?: ""
        }
    }
}

/* ******************** Platform distribution ******************** */

distributions.shadow {
    distributionBaseName.set("mqtt-cli")
    contents {
        from("README.txt")
    }
}

/* ******************** HiveMQ composite build ******************** */

val deleteOldSpecs by tasks.registering(Delete::class) {
    delete {
        fileTree("specs") {
            include("**/*.yaml")
        }
    }
}

if (gradle.includedBuilds.find { it.name == "hivemq-enterprise" } != null) {
    tasks.register<Copy>("copyHiveMQSpec") {
        dependsOn(gradle.includedBuild("hivemq-enterprise").task(":openApiSpec"))
        mustRunAfter(deleteOldSpecs)

        from("../hivemq-enterprise/build/openapi") {
            include { ".*$version-OpenAPI-spec.yaml".toRegex().matches(it.name) }
        }
        into("specs")
    }
}

if (gradle.includedBuilds.find { it.name == "hivemq-enterprise" } != null) {
    tasks.register("updateSpecs") {
        dependsOn(deleteOldSpecs)
        dependsOn(tasks.named("copyHiveMQSpec"))

        doLast {
            val gradleProperties = file("$projectDir/gradle.properties")
            var text = gradleProperties.readText()

            text = text.replace("(?m)^hivemq-api\\.version=.+".toRegex(), "hivemq-api.version=$version")

            gradleProperties.writeText(text)
        }
    }
}

if (gradle.includedBuilds.find { it.name == "plugins" } != null) {
    apply(plugin = "com.hivemq.version-updater")
}

/* ******************** helpers ******************** */

fun sha256Hash(file: File): String {
    val bytes = file.readBytes()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}