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

plugins {
    java
    application
    id("com.github.johnrengelman.shadow")
    id("io.github.sgtsilvio.gradle.defaults")
    id("nebula.ospackage")
    id("edu.sc.seis.launch4j")
    id("org.openapi.generator")
    id("com.google.cloud.tools.jib")
    id("com.github.hierynomus.license")
    id("de.thetaphi.forbiddenapis")
    id("com.github.breadmoirai.github-release")
    id("org.ajoberstar.git-publish")
    id("org.owasp.dependencycheck")
    id("com.github.ben-manes.versions")
    id("org.graalvm.buildtools.native")
    id("com.hivemq.cli.native-image")
}

/* ******************** metadata ******************** */

val prevVersion = "4.8.2"
version = "4.8.3"
group = "com.hivemq"
description = "MQTT CLI is a tool that provides a feature rich command line interface for connecting, " +
        "publishing, subscribing, unsubscribing and disconnecting " +
        "various MQTT clients simultaneously and supports  MQTT 5.0 and MQTT 3.1.1 "

application {
    mainClass.set("com.hivemq.cli.MqttCLIMain")
}

/* ******************** java ******************** */

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
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

tasks.jar {
    archiveClassifier.set("plain")
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
    implementation("com.google.code.findbugs:jsr305:${property("find-bugs.version")}")
    implementation("com.squareup.okhttp3:okhttp:${property("ok-http.version")}")
    implementation("com.squareup.okhttp3:logging-interceptor:${property("ok-http.version")}")
    implementation("io.gsonfire:gson-fire:${property("gson-fire.version")}")
    implementation("org.apache.commons:commons-lang3:${property("commons-lang.version")}")
    implementation("javax.annotation:javax.annotation-api:${property("javax.version")}")

    implementation("org.jline:jline:${property("jline.version")}")
    implementation("org.jline:jline-terminal-jansi:${property("jline.version")}")
    implementation("com.google.dagger:dagger:${property("dagger.version")}")
    compileOnly("org.graalvm.nativeimage:svm:${property("substrate-vm.version")}")
    annotationProcessor("com.google.dagger:dagger-compiler:${property("dagger.version")}")

    implementation("info.picocli:picocli:${property("picocli.version")}")
    implementation("info.picocli:picocli-shell-jline3:${property("picoclishell.version")}")
    implementation("info.picocli:picocli-codegen:${property("picocli.version")}")
    annotationProcessor("info.picocli:picocli-codegen:${property("picocli.version")}")
    implementation("com.google.guava:guava:${property("guava.version")}")
    implementation("com.google.code.gson:gson:${property("gson.version")}")
    implementation("commons-io:commons-io:${property("commons-io.version")}")
    implementation("org.tinylog:tinylog-api:${property("tinylog.version")}")
    implementation("org.tinylog:tinylog-impl:${property("tinylog.version")}")
    implementation("org.jetbrains:annotations:${property("jetbrains-annotations.version")}")
    implementation("org.bouncycastle:bcprov-jdk15on:${property("bouncycastle.version")}")
    implementation("org.bouncycastle:bcpkix-jdk15on:${property("bouncycastle.version")}")
    implementation("com.hivemq:hivemq-mqtt-client:${property("hivemq-client.version")}")
    implementation("io.netty:netty-handler:${property("netty.version")}")
    implementation("io.netty:netty-codec-http:${property("netty.version")}")
    implementation("io.netty:netty-transport-native-epoll:${property("netty.version")}:linux-x86_64")
    implementation("com.opencsv:opencsv:${property("open-csv.version")}")
}

/* ******************** OpenAPI ******************** */

val hivemqOpenApi: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val hivemqOpenApiFromProject: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named("open-api"))
    }
}

val swarmOpenApi: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val swarmOpenApiFromProject: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named("open-api"))
    }
}

dependencies {
    hivemqOpenApi(files("specs/hivemq-openapi.yaml"))
    swarmOpenApi(files("specs/hivemq-swarm-openapi.yaml"))

    hivemqOpenApiFromProject("com.hivemq:hivemq-enterprise")
    swarmOpenApiFromProject("com.hivemq:hivemq-swarm")
}

val generateHivemqOpenApi by tasks.registering(GenerateTask::class) {
    group = "hivemq"
    generatorName.set("java")
    inputSpec.set(hivemqOpenApi.singleFile.path)
    outputDir.set("$buildDir/tmp/$name")
    apiPackage.set("com.hivemq.cli.openapi.hivemq")
    modelPackage.set("com.hivemq.cli.openapi.hivemq")
    configOptions.put("dateLibrary", "java8")
    configOptions.put("hideGenerationTimestamp", "true")

    inputs.file(hivemqOpenApi.elements.map { it.first() }).withPropertyName("inputSpec")
        .withPathSensitivity(PathSensitivity.NONE)
    val outputSrcDir = layout.buildDirectory.dir("generated/openapi/hivemq/java")
    outputs.dir(outputSrcDir).withPropertyName("outputSrcDir")
    outputs.cacheIf { true }
    doFirst { delete(outputDir) }
    doLast {
        sync {
            from("${outputDir.get()}/src/main/java")
            into(outputSrcDir)
        }
    }
}

val generateSwarmOpenApi by tasks.registering(GenerateTask::class) {
    group = "swarm"
    generatorName.set("java")
    inputSpec.set(swarmOpenApi.singleFile.path)
    outputDir.set("$buildDir/tmp/$name")
    apiPackage.set("com.hivemq.cli.openapi.swarm")
    modelPackage.set("com.hivemq.cli.openapi.swarm")
    configOptions.put("dateLibrary", "java8")
    configOptions.put("hideGenerationTimestamp", "true")

    inputs.file(swarmOpenApi.elements.map { it.first() }).withPropertyName("inputSpec")
        .withPathSensitivity(PathSensitivity.NONE)
    val outputSrcDir = layout.buildDirectory.dir("generated/openapi/swarm/java")
    outputs.dir(outputSrcDir).withPropertyName("outputSrcDir")
    outputs.cacheIf { true }
    doFirst { delete(outputDir) }
    doLast {
        sync {
            from("${outputDir.get()}/src/main/java")
            into(outputSrcDir)
            include("${apiPackage.get().replace('.', '/')}/**")
        }
    }
}

sourceSets.main {
    java {
        srcDir(generateHivemqOpenApi)
        srcDir(generateSwarmOpenApi)
    }
}

tasks.register<Sync>("updateOpenApiSpecs") {
    group = "openapi"
    from(hivemqOpenApiFromProject) { rename { "hivemq-openapi.yaml" } }
    from(swarmOpenApiFromProject) { rename { "hivemq-swarm-openapi.yaml" } }
    into("specs")
}

/* ******************** test ******************** */

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:${property("junit-jupiter.version")}")
    testImplementation("org.mockito:mockito-core:${property("mockito.version")}")
    testImplementation("com.squareup.okhttp3:mockwebserver:${property("ok-http.version")}")
    testImplementation("com.ginsberg:junit5-system-exit:${property("system-exit.version")}")
}

/* ******************** integration Tests ******************** */

sourceSets.create("integrationTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    integrationTestImplementation("com.hivemq:hivemq-testcontainer-junit5:${property("hivemq-testcontainer.version")}")
    integrationTestImplementation("org.testcontainers:testcontainers:${property("testcontainers.version")}")
    integrationTestImplementation("org.awaitility:awaitility:${property("awaitility.version")}")
}

val integrationTest by tasks.registering(Test::class) {
    group = "verification"
    description = "Runs integration tests."
    testClassesDirs = sourceSets[name].output.classesDirs
    classpath = sourceSets[name].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}

tasks.check { dependsOn(integrationTest) }

/* ******************** system Tests ******************** */

sourceSets.create("systemTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val systemTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val systemTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    systemTestImplementation("com.hivemq:hivemq-testcontainer-junit5:${property("hivemq-testcontainer.version")}")
    systemTestImplementation("org.testcontainers:testcontainers:${property("testcontainers.version")}")
}

tasks.named<JavaCompile>("compileSystemTestJava") {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(11))
    })
}

val systemTest by tasks.registering(Test::class) {
    group = "verification"
    description = "Runs system tests."
    useJUnitPlatform()
    testClassesDirs = sourceSets["systemTest"].output.classesDirs
    classpath = sourceSets["systemTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(11))
    })
    dependsOn(tasks.shadowJar)
    systemProperties["cliExec"] = javaLauncher.get().executablePath.asFile.absolutePath + " -jar " +
            tasks.shadowJar.map { it.outputs.files.singleFile }.get()
}

val systemTestNative by tasks.registering(Test::class) {
    group = "verification"
    description = "Runs native system tests."
    useJUnitPlatform()
    testClassesDirs = sourceSets["systemTest"].output.classesDirs
    classpath = sourceSets["systemTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(11))
    })
    dependsOn(tasks.nativeCompile)
    systemProperties["cliExec"] =
        tasks.nativeCompile.map { it.outputs.files.singleFile }.get().resolve(project.name).absolutePath
    testLogging {
        showCauses = true
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
    }
}

tasks.check { dependsOn(systemTest, systemTestNative) }

/* ******************** compliance ******************** */

license {
    header = file("HEADER")
    include("**/*.java")
    exclude("**/com/hivemq/cli/openapi/**")
    mapping("java", "SLASHSTAR_STYLE")
}

downloadLicenses {
    aliases = mapOf(
        license("Apache License, Version 2.0", "https://opensource.org/licenses/Apache-2.0") to listOf(
            "Apache 2",
            "Apache 2.0",
            "Apache-2.0",
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
            "The Apache Software License, Version 2.0",
            "The Apache Software License, version 2.0"
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
            classpath("gradle/tools/license-third-party-tool-3.0.jar")
            args(
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

tasks.named("forbiddenApisIntegrationTest") { enabled = false }

/* ******************** graal ******************** */

cliNative {
    graalVersion.set(property("graal.version").toString())
    javaVersion.set(property("java-native.version").toString())
}

/*tasks.nativeCompile {
    dependsOn(tasks.installNativeImageTooling)
}*/

val agentMainRun by tasks.registering(JavaExec::class) {
    group = "native"

    val launcher = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(project.property("java-native.version").toString()))
        vendor.set(JvmVendorSpec.GRAAL_VM)

    }
    javaLauncher.set(launcher)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.hivemq.cli.graal.NativeMain")
}

val nativeImageOptions by graalvmNative.binaries.named("main") {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(property("java-native.version").toString()))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    })
    buildArgs.add("-Dio.netty.noUnsafe=true")
    buildArgs.add("-H:+ReportExceptionStackTraces")
    buildArgs.add("-H:+TraceServiceLoaderFeature")
    buildArgs.add("--no-fallback")
    buildArgs.add("--enable-https")
    buildArgs.add(
        "--initialize-at-build-time=" +
                "org.jctools.queues.BaseMpscLinkedArrayQueue," +
                "org.jctools.queues.BaseSpscLinkedArrayQueue," +
                "org.jctools.util.UnsafeAccess," +
                "io.netty.util.ReferenceCountUtil," +
                "io.netty.util.ResourceLeakDetector," +
                "io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueue," +
                "io.netty.util.internal.shaded.org.jctools.queues.BaseSpscLinkedArrayQueue," +
                "io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess," +
                "io.netty.util.CharsetUtil," +
                "io.netty.util.internal.SystemPropertyUtil," +
                "io.netty.util.internal.PlatformDependent," +
                "io.netty.util.internal.PlatformDependent0," +
                "io.netty.util.internal.logging.JdkLogger," +
                "io.netty.buffer.AbstractByteBufAllocator"
    )
    buildArgs.add(
        "--initialize-at-run-time=" +
                "io.netty," +
                "io.netty.bootstrap," +
                "io.netty.channel," +
                "io.netty.handler.ssl," +
                "io.netty.handler.proxy," +
                "io.netty.handler.codec," +
                "io.netty.handler.codec.http," +
                "io.netty.internal.tcnative," +
                "io.netty.resolver," +
                "io.netty.util," +
                "io.netty.util.concurrent," +
                "org.tinylog," +
                "org.tinylog.configuration," +
                "org.tinylog.format," +
                "org.tinylog.provider," +
                "org.tinylog.runtime," +
                "org.tinylog.converters," +
                "org.tinylog.core," +
                "org.tinylog.path," +
                "org.tinylog.pattern," +
                "org.tinylog.policies," +
                "org.tinylog.throwable," +
                "org.tinylog.writers," +
                "org.tinylog.writers.raw"
    )
}

graalvmNative {
    toolchainDetection.set(false)
    agent {
        tasksToInstrumentPredicate.set { t -> t == agentMainRun.get() }
    }
    binaries {
        nativeImageOptions
    }
}

/* ******************** homebrew package & formula ******************** */

val buildBrewZip by tasks.registering(Zip::class) {

    archiveClassifier.set("brew")
    destinationDirectory.set(layout.buildDirectory.dir("packages/homebrew"))

    into("brew") {
        from(tasks.shadowJar)
        from("packages/homebrew/mqtt")
    }
    from("LICENSE") {
        into("licenses")
    }
}

val buildBrewFormula by tasks.registering(Copy::class) {
    dependsOn(buildBrewZip)

    from("packages/homebrew/mqtt-cli.rb")
    into(layout.buildDirectory.dir("packages/homebrew"))
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
    from("packages/linux/mqtt", closureOf<CopySpec> {
        fileMode = 0b111_101_101 // 0755
        filter {
            it.replace("@@jarPath@@", "/opt/$packageName/${tasks.shadowJar.get().archiveFileName.get()}")
        }
    })
    from("LICENSE", closureOf<CopySpec> {
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
    into(layout.buildDirectory.dir("packages/debian"))
    rename { "${project.name}-${project.version}.deb" }
}

val buildRpmPackage by tasks.registering(Copy::class) {
    from(tasks.buildRpm.flatMap { it.archiveFile })
    into(layout.buildDirectory.dir("packages/rpm"))
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
    destinationDirectory.set(layout.buildDirectory.dir("packages/windows"))

    from("packages/windows") {
        filter { it.replace("@@exeName@@", launch4j.outfile) }
    }
    from(tasks.createExe.map { it.dest })
    from("LICENSE")
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

/* ******************** platform distribution ******************** */

distributions.shadow {
    distributionBaseName.set("mqtt-cli")
    contents {
        from("README.txt")
    }
}

tasks.startShadowScripts {
    applicationName = "mqtt"
}

/* ******************** version updating ******************** */

val updateVersionInFiles by tasks.registering {
    group = "version"
    val filesToUpdate = files("docs/_docs/installation.md")

    doLast {
        filesToUpdate.forEach {
            val text = it.readText()
            val replacedText = text.replace("${prevVersion}(-SNAPSHOT)?".toRegex(), project.version.toString())
            it.writeText(replacedText)
        }
    }
}

/* ******************** helpers ******************** */

fun sha256Hash(file: File): String {
    val bytes = file.readBytes()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

/* ******************** artifacts ******************** */

val releaseBinary: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named("binary"))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("release"))
    }
}

artifacts {
    add(releaseBinary.name, tasks.shadowDistZip)
}

dependencyCheck {
    scanConfigurations = listOf("runtimeClasspath")
}