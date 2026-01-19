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
    alias(libs.plugins.oci)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.license)
    alias(libs.plugins.forbiddenApis)
    alias(libs.plugins.githubRelease)
    alias(libs.plugins.gitPublish)
    alias(libs.plugins.graalvm.native)
    id("com.hivemq.cli.native-image")
}

/* ******************** metadata ******************** */

val prevVersion = "4.46.0"
version = "4.47.0"
group = "com.hivemq"
description = "MQTT CLI is a tool that provides a feature rich command line interface for connecting, " + //
        "publishing, subscribing, unsubscribing and disconnecting " + //
        "various MQTT clients simultaneously and supports  MQTT 5.0 and MQTT 3.1.1 "

application {
    mainClass = "com.hivemq.cli.MqttCLIMain"
}

/* ******************** java ******************** */

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.compileJava {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

tasks.jar {
    manifest.attributes(
        "Built-JDK" to System.getProperty("java.version"),
        "Implementation-Title" to "MQTT CLI",
        "Implementation-Version" to provider { project.version.toString() },
        "Implementation-Vendor" to "HiveMQ GmbH",
        "Specification-Title" to "MQTT CLI",
        "Specification-Version" to provider { project.version.toString() },
        "Specification-Vendor" to "HiveMQ GmbH",
        "Main-Class" to application.mainClass,
        "Built-Date" to SimpleDateFormat("yyyy-MM-dd").format(Date()),
    )
}

tasks.jar {
    archiveClassifier = "plain"
}

tasks.shadowJar {
    archiveClassifier = ""
}

/* ******************** dependencies ******************** */

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.swagger.annotations)
    implementation(libs.jsr305)
    implementation(platform(libs.okhttp.bom))
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
    group = "openapi"
    generatorName = "java"
    inputSpec = hivemqOpenApi.elements.map { it.single().asFile.path }
    val outputDir = layout.buildDirectory.dir("tmp/$name")
    this.outputDir = outputDir.map { it.asFile.absolutePath }
    cleanupOutput = true

    apiPackage = "com.hivemq.cli.openapi.hivemq"
    modelPackage = "com.hivemq.cli.openapi.hivemq"
    modelNamePrefix = "HivemqOpenapi"
    configOptions.put("hideGenerationTimestamp", "true")
    configOptions.put("openApiNullable", "false")

    val outputSrcDir = layout.buildDirectory.dir("generated/openapi/hivemq/java")
    outputs.dir(outputSrcDir).withPropertyName("outputSrcDir")
    outputs.cacheIf { true }

    doLast {
        outputSrcDir.get().asFile.deleteRecursively()
        outputDir.get().asFile.resolve("src/main/java").copyRecursively(outputSrcDir.get().asFile)
        outputDir.get().asFile.deleteRecursively()
    }
}

val generateSwarmOpenApi by tasks.registering(GenerateTask::class) {
    group = "openapi"
    generatorName = "java"
    inputSpec = swarmOpenApi.elements.map { it.single().asFile.path }
    val outputDir = layout.buildDirectory.dir("tmp/$name")
    this.outputDir = outputDir.map { it.asFile.absolutePath }
    cleanupOutput = true

    apiPackage = "com.hivemq.cli.openapi.swarm"
    modelPackage = "com.hivemq.cli.openapi.swarm"
    configOptions.put("hideGenerationTimestamp", "true")
    configOptions.put("openApiNullable", "false")

    val outputSrcDir = layout.buildDirectory.dir("generated/openapi/swarm/java")
    outputs.dir(outputSrcDir).withPropertyName("outputSrcDir")
    outputs.cacheIf { true }

    doLast {
        outputSrcDir.get().asFile.deleteRecursively()
        val path = apiPackage.get().replace('.', '/')
        outputDir.get().asFile.resolve("src/main/java/$path").copyRecursively(outputSrcDir.get().asFile.resolve(path))
        outputDir.get().asFile.deleteRecursively()
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

@Suppress("UnstableApiUsage") //
testing {
    suites {
        withType<JvmTestSuite> {
            useJUnitJupiter(libs.versions.junit.jupiter)
        }

        @Suppress("unused")
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(libs.mockito)
                implementation(platform(libs.okhttp.bom))
                implementation(libs.okhttp.mockWebserver)
            }
        }

        val integrationTest by registering(JvmTestSuite::class) {
            testType = TestSuiteType.INTEGRATION_TEST

            dependencies {
                runtimeOnly(libs.junit.platformLauncher)
                implementation(libs.awaitility)
                implementation(libs.gradleOci.junitJupiter)
                implementation(libs.mockito)
                implementation(libs.testcontainers.junitJupiter)
                implementation(libs.testcontainers.hivemq)

                implementation(libs.dagger)
                implementation(libs.gson)
                implementation(libs.hivemq.mqttClient)
                implementation(platform(libs.okhttp.bom))
                implementation(libs.okhttp)
                implementation(libs.openCsv)
                implementation(libs.picocli)
                implementation(libs.tinylog.api)
                implementation(project())
            }

            oci.of(this) {
                imageDependencies {
                    runtime("hivemq:hivemq4:latest") { isChanging = true }
                    runtime("hivemq:hivemq-swarm:latest") { isChanging = true }
                }
                val linuxAmd64 = platformSelector(platform("linux", "amd64"))
                val linuxArm64v8 = platformSelector(platform("linux", "arm64", "v8"))
                platformSelector = if (System.getenv("CI_RUN") != null) linuxAmd64 else linuxAmd64.and(linuxArm64v8)
            }
        }

        val systemTest by registering(JvmTestSuite::class) {
            testType = TestSuiteType.FUNCTIONAL_TEST
            targets {
                all {
                    testTask {
                        systemProperties["junit.jupiter.testinstance.lifecycle.default"] = "per_class"
                    }
                }
                named("systemTest") {
                    testTask {
                        dependsOn(tasks.shadowJar)
                        systemProperties["cliExec"] = "${javaLauncher.get().executablePath.asFile.absolutePath} -jar ${
                            tasks.shadowJar.get().archiveFile.get()
                        }"
                    }
                }
                register("systemTestNative") {
                    testTask {
                        dependsOn(tasks.nativeCompile)
                        systemProperties["cliExec"] = tasks.nativeCompile.get().outputFile.get().toString()
                    }
                }
            }

            dependencies {
                implementation(libs.junit.platformLauncher)
                implementation(libs.awaitility)
                implementation(libs.gradleOci.junitJupiter)
                implementation(libs.hivemq.communityEditionEmbedded)
                implementation(libs.junit.pioneer)
                implementation(libs.testcontainers)

                implementation(libs.apache.commonsIO)
                implementation(libs.gson)
                implementation(libs.guava)
                implementation(libs.hivemq.mqttClient)
            }

            oci.of(this) {
                imageDependencies {
                    runtime(project).tag("latest")
                }
                val linuxAmd64 = platformSelector(platform("linux", "amd64"))
                val linuxArm64v8 = platformSelector(platform("linux", "arm64", "v8"))
                platformSelector = if (System.getenv("CI_RUN") != null) linuxAmd64 else linuxAmd64.and(linuxArm64v8)
            }
        }

        tasks.named("check") {
            dependsOn(integrationTest, systemTest)
        }
    }
}


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
    javaVersion = libs.versions.javaNative
}
val majorJavaNativeVersion = libs.versions.javaNative.get().substringBefore(".")

//reflection configuration files are currently created manually with the command: ./gradlew -Pagent agentMainRun --stacktrace
//this yields an exception as the Graal plugin is currently quite buggy. The files are created nonetheless.
//build/native/agent-output/agentMainRun/session-*****-*Date*T*Time*Z -> src/main/resources/META-INF/native-image
val agentMainRun by tasks.registering(JavaExec::class) {
    group = "native"

    val launcher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(majorJavaNativeVersion)
        vendor = JvmVendorSpec.GRAAL_VM
    }
    javaLauncher = launcher
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.hivemq.cli.graal.NativeMain"
}

val nativeImageOptions by graalvmNative.binaries.named("main") {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(majorJavaNativeVersion)
        vendor = JvmVendorSpec.GRAAL_VM
    }
    buildArgs.add("-Dio.netty.noUnsafe=true")
    buildArgs.add("-Dio.netty.jfr.enabled=false")
    buildArgs.add("--no-fallback")
    buildArgs.add("--enable-https")
    buildArgs.add("--features=com.hivemq.cli.graal.BouncyCastleFeature")
    //@formatter:off
    buildArgs.add(
        "--initialize-at-build-time=" +
                "org.bouncycastle," +
                "org.jctools.queues.BaseMpscLinkedArrayQueue," +
                "org.jctools.queues.BaseSpscLinkedArrayQueue," +
                "org.jctools.util.UnsafeAccess," +
                "io.netty.util.ReferenceCountUtil," +
                "io.netty.util.ResourceLeakDetector," +
                "io.netty.util.ResourceLeakDetector\$Level," +
                "io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueue," +
                "io.netty.util.internal.shaded.org.jctools.queues.BaseSpscLinkedArrayQueue," +
                "io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess," +
                "io.netty.util.CharsetUtil," +
                "io.netty.util.internal.SystemPropertyUtil," +
                "io.netty.util.internal.PlatformDependent," +
                "io.netty.util.internal.PlatformDependent0," +
                "io.netty.util.internal.PlatformDependent\$1," +
                "io.netty.util.internal.PlatformDependent\$2," +
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
    //@formatter:on
}

graalvmNative {
    toolchainDetection = false
    agent {
        defaultMode = "standard"
        tasksToInstrumentPredicate.set { t -> t == agentMainRun.get() }
    }
    binaries {
        nativeImageOptions
    }
}

/* ******************** homebrew package & formula ******************** */

val buildBrewZip by tasks.registering(Zip::class) {

    archiveClassifier = "brew"
    destinationDirectory = layout.buildDirectory.dir("packages/homebrew")

    into("brew") {
        from(tasks.shadowJar)
        from("packages/homebrew/mqtt")
    }
    from("LICENSE") {
        into("licenses")
    }
}

val buildBrewFormula by tasks.registering {
    val inputFile = layout.projectDirectory.file("packages/homebrew/mqtt-cli.rb")
    inputs.file(inputFile).withPropertyName("inputFile").withPathSensitivity(PathSensitivity.NONE)
    val description = provider { project.description!! }
    inputs.property("description", description)
    val version = provider { project.version.toString() }
    inputs.property("version", version)
    val archiveFileName = buildBrewZip.flatMap { it.archiveFileName }
    inputs.property("archiveFileName", archiveFileName)
    val archiveFile = buildBrewZip.flatMap { it.archiveFile }
    inputs.file(archiveFile).withPropertyName("archiveFile").withPathSensitivity(PathSensitivity.NONE)
    val outputFile = layout.buildDirectory.file("packages/homebrew/mqtt-cli.rb")
    outputs.file(outputFile).withPropertyName("outputFile")
    doLast {
        val archiveBytes = archiveFile.get().asFile.readBytes()
        val archiveDigest = MessageDigest.getInstance("SHA-256").digest(archiveBytes)
        val archiveChecksum = archiveDigest.fold("") { string, b -> string + "%02x".format(b) }
        val content = inputFile.asFile.readText() //
            .replace("@@description@@", description.get()) //
            .replace("@@version@@", version.get()) //
            .replace("@@filename@@", archiveFileName.get()) //
            .replace("@@shasum@@", archiveChecksum)
        outputFile.get().asFile.writeText(content)
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

    val destinationPath = "/opt/$packageName"
    into(destinationPath)
    from(tasks.shadowJar)
    val jarFileName = tasks.shadowJar.flatMap { it.archiveFileName }
    from("packages/linux/mqtt", closureOf<CopySpec> {
        filePermissions { unix(0b111_101_101) }
        filter { it.replace("@@jarPath@@", "$destinationPath/${jarFileName.get()}") }
    })
    from("LICENSE", closureOf<CopySpec> {
        into("licenses")
        CopySpecEnhancement.fileType(this, Directive.LICENSE)
    })

    link("/usr/bin/mqtt", "$destinationPath/mqtt", 0b111_101_101)
}

tasks.buildDeb {
    requires("java8-runtime").or("java8-runtime-headless")
}

tasks.buildRpm {
    release = "1"
    requires("jre", "1.8.0", Flags.GREATER or Flags.EQUAL)
}

val buildDebianPackage by tasks.registering {
    val inputFile = tasks.buildDeb.flatMap { it.archiveFile }
    inputs.file(inputFile).withPropertyName("inputFile").withPathSensitivity(PathSensitivity.NONE)
    val outputFile = layout.buildDirectory.file(provider { "packages/debian/${project.name}-${project.version}.deb" })
    outputs.file(outputFile).withPropertyName("outputFile")
    doLast {
        inputFile.get().asFile.copyTo(outputFile.get().asFile, true)
    }
}

val buildRpmPackage by tasks.registering {
    val inputFile = tasks.buildRpm.flatMap { it.archiveFile }
    inputs.file(inputFile).withPropertyName("inputFile").withPathSensitivity(PathSensitivity.NONE)
    val outputFile = layout.buildDirectory.file(provider { "packages/rpm/${project.name}-${project.version}.rpm" })
    outputs.file(outputFile).withPropertyName("outputFile")
    doLast {
        inputFile.get().asFile.copyTo(outputFile.get().asFile, true)
    }
}

/* ******************** windows zip ******************** */

launch4j {
    headerType = "console"
    mainClassName = application.mainClass
    icon = "$projectDir/icons/05-mqtt-cli-icon.ico"
    setJarTask(tasks.shadowJar.map { it })
    copyConfigurable = emptyList<Any>()
    copyright = "Copyright 2019-present HiveMQ and the HiveMQ Community"
    companyName = "HiveMQ GmbH"
    downloadUrl = "https://openjdk.java.net/install/"
    jreMinVersion = "1.8"
    windowTitle = "MQTT CLI"
    version = provider { project.version.toString() }
    textVersion = provider { project.version.toString() }
}

val buildWindowsZip by tasks.registering(Zip::class) {
    archiveClassifier = "win"
    destinationDirectory = layout.buildDirectory.dir("packages/windows")

    val exeFileName = launch4j.outfile
    from("packages/windows") {
        filter { it.replace("@@exeName@@", exeFileName.get()) }
    }
    from(tasks.createExe.map { it.dest })
    from("LICENSE")
}

/* ******************** packages ******************** */

val buildPackages by tasks.registering {
    dependsOn(buildBrewFormula, buildDebianPackage, buildRpmPackage, buildWindowsZip)
}

/* ******************** Attach all packages to GitHub release ******************** */

githubRelease {
    token(System.getenv("githubToken"))
    releaseAssets(tasks.shadowJar, buildBrewZip, buildDebianPackage, buildRpmPackage, buildWindowsZip)
    allowUploadToExisting = true
}

/* ******************** Update the Homebrew-Formula with the newly built package ******************** */

gitPublish {
    repoUri = "https://github.com/hivemq/homebrew-mqtt-cli.git"
    branch = "master"
    commitMessage = "Release version v${project.version}"
    contents.from(buildBrewFormula)
}

/* ******************** docker ******************** */

oci {
    registries {
        dockerHub {
            optionalCredentials()
        }
    }
    imageDefinitions.register("main") {
        allPlatforms {
            dependencies {
                runtime("library:eclipse-temurin:sha256!d3eb69add1874bc785382d6282db53a67841f602a1139dee6c4a1221d8c56568") // 21-jre-noble
            }
            config {
                entryPoint.add("java")
                entryPoint.addAll(application.applicationDefaultJvmArgs)
                entryPoint.addAll("-cp", "/app/classpath/*:/app/libs/*")
                entryPoint.add(application.mainClass)
            }
            layer("libs") {
                contents {
                    into("app/libs") {
                        from(configurations.runtimeClasspath)
                    }
                }
            }
            layer("jar") {
                contents {
                    into("app/classpath") {
                        from(tasks.jar)
                        rename(".*", "${project.name}-${project.version}.jar")
                    }
                }
            }
            layer("resources") {
                contents {
                    into("app") {
                        from("src/distribution")
                    }
                }
            }
        }
        specificPlatform(platform("linux", "amd64"))
        specificPlatform(platform("linux", "arm64", "v8"))
    }
}

/* ******************** platform distribution ******************** */

distributions.shadow {
    distributionBaseName = "mqtt-cli"
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
