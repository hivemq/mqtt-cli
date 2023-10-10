import com.netflix.gradle.plugins.packaging.CopySpecEnhancement
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.redline_rpm.header.Flags
import org.redline_rpm.header.Os
import org.redline_rpm.header.RpmType
import org.redline_rpm.payload.Directive
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

buildscript {
    if (gradle.includedBuilds.any { it.name == "plugins" }) {
        plugins {
            id("com.hivemq.third-party-license-generator")
        }
    }
}

plugins {
    java
    application
    alias(libs.plugins.shadow)
    alias(libs.plugins.defaults)
    alias(libs.plugins.nebula.ospackage)
    alias(libs.plugins.launch4j)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.jib)
    alias(libs.plugins.license)
    alias(libs.plugins.forbiddenApis)
    alias(libs.plugins.githubRelease)
    alias(libs.plugins.gitPublish)
    alias(libs.plugins.graalvm.native)
    id("com.hivemq.cli.native-image")
}

/* ******************** metadata ******************** */

val prevVersion = "4.20.0"
version = "4.21.0"
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
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.compileJava {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    })
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
        "Built-Date" to SimpleDateFormat("yyyy-MM-dd").format(Date()),
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
    implementation(libs.swagger.annotations)
    implementation(libs.findbugs)
    implementation(libs.okhttp)
    implementation(libs.okhttp.loggingInterceptor)
    implementation(libs.gsonFire)
    implementation(libs.apache.commonsLang)
    implementation(libs.javax.annotation.api)

    implementation(libs.jline)
    implementation(libs.jline.terminal.jansi)
    implementation(libs.dagger)
    compileOnly(libs.graalvm.nativeImage.svm)
    annotationProcessor(libs.dagger.compiler)

    implementation(libs.picocli)
    implementation(libs.picocli.shellJline)
    implementation(libs.picocli.codegen)
    annotationProcessor(libs.picocli.codegen)
    implementation(libs.guava)
    implementation(libs.gson)
    implementation(libs.apache.commonsIO)
    implementation(libs.tinylog.api)
    implementation(libs.tinylog.impl)
    implementation(libs.jetbrains.annotations)
    implementation(libs.bouncycastle.prov)
    implementation(libs.bouncycastle.pkix)
    implementation(libs.hivemq.mqttClient)
    implementation(libs.netty.handler)
    implementation(libs.netty.codec.http)
    implementation(variantOf(libs.netty.transport.native.epoll) { classifier("linux-x86_64") })
    implementation(libs.openCsv)
    constraints {
        implementation(libs.apache.commonsText) {
            because(
                "Force a commons-text version that does not contain CVE-2022-42889, " +
                        "because opencsv brings the vulnerable version 1.9 as transitive dependency"
            )
        }
    }
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

    hivemqOpenApiFromProject(libs.hivemq.enterprise)
    swarmOpenApiFromProject(libs.hivemq.swarm)
}

val generateHivemqOpenApi by tasks.registering(GenerateTask::class) {
    group = "hivemq"
    generatorName.set("java")
    inputSpec.set(hivemqOpenApi.singleFile.path)
    outputDir.set(layout.buildDirectory.dir("tmp/$name").get().asFile.absolutePath)
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
    outputDir.set(layout.buildDirectory.dir("tmp/$name").get().asFile.absolutePath)
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
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito)
    testImplementation(libs.okhttp.mockWebserver)
    testImplementation(libs.junit.systemExit)

    modules {
        module("org.bouncycastle:bcpkix-jdk15on") { replacedBy("org.bouncycastle:bcpkix-jdk18on") }
        module("org.bouncycastle:bcprov-jdk15on") { replacedBy("org.bouncycastle:bcprov-jdk18on") }
        module("org.bouncycastle:bcutil-jdk15on") { replacedBy("org.bouncycastle:bcutil-jdk18on") }
    }
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
    integrationTestImplementation(libs.hivemq.testcontainer.junit5)
    integrationTestImplementation(libs.testcontainers)
    integrationTestImplementation(libs.awaitility)
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

sourceSets.create("systemTest")

val systemTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val systemTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    systemTestImplementation(libs.hivemq.testcontainer.junit5)
    systemTestImplementation(libs.testcontainers)
    systemTestImplementation(libs.awaitility)
    systemTestImplementation(libs.hivemq.communityEditionEmbedded)
    systemTestImplementation(libs.junit.pioneer)
}

val systemTest by tasks.registering(Test::class) {
    group = "verification"
    description = "Runs system tests."
    testClassesDirs = sourceSets["systemTest"].output.classesDirs
    classpath = sourceSets["systemTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
    dependsOn(tasks.shadowJar)
    systemProperties["junit.jupiter.testinstance.lifecycle.default"] = "per_class"
    systemProperties["cliExec"] = javaLauncher.get().executablePath.asFile.absolutePath + " -jar " +
            tasks.shadowJar.map { it.outputs.files.singleFile }.get()
    systemProperties["java"] = javaLauncher.get().executablePath.asFile.absolutePath
}

val systemTestNative by tasks.registering(Test::class) {
    group = "verification"
    description = "Runs native system tests."
    testClassesDirs = sourceSets["systemTest"].output.classesDirs
    classpath = sourceSets["systemTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
    dependsOn(tasks.nativeCompile)
    systemProperties["junit.jupiter.testinstance.lifecycle.default"] = "per_class"
    systemProperties["cliExec"] =
        tasks.nativeCompile.map { it.outputs.files.singleFile }.get().resolve(project.name).absolutePath
    systemProperties["java"] = javaLauncher.get().executablePath.asFile.absolutePath
    testLogging {
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
    dependencyConfiguration = "runtimeClasspath"
}

tasks.downloadLicenses {
    dependsOn(tasks.clean)
}

plugins.withId("com.hivemq.third-party-license-generator") {
    tasks.named("updateThirdPartyLicenses") {
        dependsOn(tasks.downloadLicenses)
        extra["projectName"] = "MQTT CLI"
        extra["dependencyLicenseDirectory"] = tasks.downloadLicenses.get().xmlDestination
        extra["outputDirectory"] = layout.projectDirectory.dir("src/distribution/third-party-licenses")
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

//In order to run the native tasks the Graal environment must be installed first.
//This can be done with the "installNativeImageTooling" Task.
//Unfortunately, this is not able to be a task dependency as it uses the gradle java provisioning service which only
//checks for java installations prior the execution.

cliNative {
    graalVersion.set(libs.versions.graal)
    javaVersion.set(libs.versions.javaNative)
}

//reflection configuration files are currently created manually with the command: ./gradlew -Pagent agentMainRun --stacktrace
//this yields an exception as the Graal plugin is currently quite buggy. The files are created nonetheless.
//build/native/agent-output/agentMainRun/session-*****-*Date*T*Time*Z -> src/main/resources/META-INF/native-image
val agentMainRun by tasks.registering(JavaExec::class) {
    group = "native"

    val launcher = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaNative.get()))
        vendor.set(JvmVendorSpec.GRAAL_VM)

    }
    javaLauncher.set(launcher)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.hivemq.cli.graal.NativeMain")
}

val nativeImageOptions by graalvmNative.binaries.named("main") {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaNative.get()))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    })
    buildArgs.add("-Dio.netty.noUnsafe=true")
    buildArgs.add("-H:+ReportExceptionStackTraces")
    buildArgs.add("-H:+TraceServiceLoaderFeature")
    buildArgs.add("--no-fallback")
    buildArgs.add("--enable-https")
    buildArgs.add("--features=com.hivemq.cli.graal.BouncyCastleFeature")
    buildArgs.add(
        "--initialize-at-build-time=" +
                "org.bouncycastle," +
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
                "org.bouncycastle.jcajce.provider.drbg.DRBG\$Default," +
                "org.bouncycastle.jcajce.provider.drbg.DRBG\$NonceAndIV," +
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
        defaultMode.set("standard")
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
    requires("java8-runtime").or("java8-runtime-headless")
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
    headerType.set("console")
    mainClassName.set(application.mainClass.get())
    icon.set("$projectDir/icons/05-mqtt-cli-icon.ico")
    setJarTask(tasks.shadowJar.get())
    copyConfigurable.set(emptyList<Any>())
    copyright.set("Copyright 2019-present HiveMQ and the HiveMQ Community")
    companyName.set("HiveMQ GmbH")
    downloadUrl.set("https://openjdk.java.net/install/")
    jreMinVersion.set("1.8")
    windowTitle.set("MQTT CLI")
    version.set(project.version.toString())
    textVersion.set(project.version.toString())
}

val buildWindowsZip by tasks.registering(Zip::class) {

    archiveClassifier.set("win")
    destinationDirectory.set(layout.buildDirectory.dir("packages/windows"))

    from("packages/windows") {
        filter { it.replace("@@exeName@@", launch4j.outfile.get()) }
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
        buildWindowsZip,
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
