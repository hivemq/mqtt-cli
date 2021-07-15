import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import nl.javadude.gradle.plugins.license.DownloadLicensesExtension.license
import org.redline_rpm.header.Os
import org.redline_rpm.header.RpmType
import org.redline_rpm.payload.Directive
import java.util.Date
import java.security.MessageDigest
import java.text.SimpleDateFormat
import org.redline_rpm.header.Architecture
import org.redline_rpm.header.Flags
import java.util.Objects.requireNonNullElse
import java.util.regex.Pattern

buildscript {
    dependencies {
        dependencies {
            if (gradle.includedBuilds.find { it.name == "plugins" } != null) {
                classpath("com.hivemq:plugins")
            }
        }
    }
}

plugins {
    java
    idea
    application
    id("com.github.johnrengelman.shadow")
    id("com.github.hierynomus.license")
    id("org.gradle.crypto.checksum")
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
    mainClassName = "com.hivemq.cli.MqttCLIMain"
}

val readableName by extra("mqtt-cli")
val appName by extra("MQTT CLI")
val appJarName by extra("$readableName.jar")
val appExe by extra("$readableName.exe")

val githubOrg by extra("hivemq")
val githubRepo by extra("mqtt-cli")
val githubUrl by extra("https://github.com/$githubOrg/$githubRepo")
val scmConnection by extra("scm:git:git://github.com/$githubOrg/$githubRepo.git")
val scmDeveloperConnection by extra("scm:git:ssh://git@github.com/$githubOrg/$githubRepo.git")
val issuesUrl by extra("$githubUrl/issues")
val docUrl by extra("https://$githubOrg.github.io/$githubRepo/")

val iconsDir by extra("$projectDir/icons")
val resDir by extra("$projectDir/res")
val dmgDir by extra("$projectDir/dmg")
val pkgDir by extra("$projectDir/packages")
val brewDir by extra("$pkgDir/homebrew")
val debDir by extra("$pkgDir/debian")
val rpmDir by extra("$pkgDir/rpm")
val winDir by extra("$pkgDir/windows")

val buildLaunch4j by extra("$buildDir/launch4j")

val buildPkgDir by extra("$buildDir/packages")
val buildBrewDir by extra("$buildPkgDir/homebrew")
val buildDebDir by extra("$buildPkgDir/debian")
val buildRpmDir by extra("$buildPkgDir/rpm")
val buildWinDir by extra("$buildPkgDir/windows")

val packagePreamble by extra("$readableName-$version")
val rpmPackageName by extra("$packagePreamble.rpm")
val debPackageName by extra("$packagePreamble.deb")
val brewZipName by extra("$packagePreamble-brew.zip")
val windowsZipName by extra("$packagePreamble-win.zip")

val hmqIco by extra("$iconsDir/05-mqtt-cli-icon.ico")
val hmqLogo by extra("$iconsDir/05-mqtt-cli-icon.png")

val copyright by extra("Copyright 2019 HiveMQ and the HiveMQ Community")
val vendor by extra("HiveMQ GmbH")
val website by extra("https://www.hivemq.com/")
val license by extra("$projectDir/LICENSE")

/* ******************** java ******************** */

tasks.compileJava {
    dependsOn(generateHiveMqOpenApi)
    dependsOn(generateSwarmOpenApi)

    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "UTF-8"
}

sourceSets {
    main {
        java {
            srcDir("$rootDir/build/generated/openapi/src/main/java")
        }
    }
}

tasks.jar {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val currentDate = sdf.format(Date())

    manifest.attributes(
        "Built-JDK" to System.getProperty("java.version"),
        "Implementation-Title" to appName,
        "Implementation-Version" to project.version,
        "Implementation-Vendor" to vendor,
        "Specification-Title" to appName,
        "Specification-Version" to project.version,
        "Specification-Vendor" to vendor,
        "Main-Class" to application.mainClass.get(),
        "Built-Date" to currentDate.toString()
    )

    finalizedBy(tasks.shadowJar)
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
    }
}

idea {
    module {
        generatedSourceDirs.add(file("build/generated/sources/annotationProcessor/java/main"))
    }
}

/* ******************** OpenAPI specs ******************** */

val generateHiveMqOpenApi by tasks.registering(GenerateTask::class) {
    group = "hivemq"
    generatorName.set("java")
    inputSpec.set("$rootDir/specs/HiveMQ-${project.property("hivemq-api.version")}-OpenAPI-spec.yaml")
    outputDir.set("$buildDir/generated/openapi")
    apiPackage.set("com.hivemq.cli.openapi.hivemq")
    modelPackage.set("com.hivemq.cli.openapi.hivemq")
    configOptions.put("dateLibrary", "java8")
}

val generateSwarmOpenApi by tasks.registering(GenerateTask::class) {
    group = "swarm"
    generatorName.set("java")
    inputSpec.set("$rootDir/specs/HiveMQ-Swarm-${project.property("hivemq-swarm-api.version")}-OpenAPI-spec.yaml")
    outputDir.set("$buildDir/generated/openapi")
    apiPackage.set("com.hivemq.cli.openapi.swarm")
    modelPackage.set("com.hivemq.cli.openapi.swarm")
    configOptions.put("dateLibrary", "java8")
}

/* ******************** dependencies ******************** */

repositories {
    mavenCentral()
}

dependencies {

    // hivemq client api dependencies
    implementation("io.swagger:swagger-annotations:${property("swagger.version")}")
    implementation("com.google.code.findbugs:jsr305:${property("findBugs.version")}")
    implementation("com.squareup.okhttp3:okhttp:${property("okHttp.version")}")
    implementation("com.squareup.okhttp3:logging-interceptor:${property("okHttp.version")}")
    implementation("io.gsonfire:gson-fire:${property("gsonFire.version")}")
    implementation("org.apache.commons:commons-lang3:${property("commonsLang.version")}")
    implementation("javax.annotation:javax.annotation-api:${property("javax.version")}")

    implementation("org.jline:jline:${property("jline3.version")}")
    implementation("org.jline:jline-terminal-jansi:${property("jline3Jansi.version")}")
    implementation("com.google.dagger:dagger:${property("dagger.version")}")
    compileOnly("com.oracle.substratevm:svm:${property("substrateVm.version")}")
    annotationProcessor("com.google.dagger:dagger-compiler:${property("dagger.version")}")

    implementation("info.picocli:picocli:${property("picocli.version")}")
    implementation("info.picocli:picocli-shell-jline3:${property("picoclishell.version")}")
    implementation("info.picocli:picocli-codegen:${property("picocli.version")}")
    implementation("com.google.guava:guava:${property("guava.version")}")
    implementation("com.google.code.gson:gson:${property("gson.version")}")
    implementation("commons-io:commons-io:${property("commonsIo.version")}")
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

    testImplementation("org.awaitility:awaitility:${property("awaitility.version")}")
    testImplementation("org.mockito:mockito-core:${property("mockito.version")}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junitJupiter.version")}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${property("junitJupiter.version")}")
    testImplementation("com.squareup.okhttp3:mockwebserver:${property("mockWebserver.version")}")
    testImplementation("com.hivemq:hivemq-testcontainer-junit5:${property("hivemqTestcontainer.version")}")
    testImplementation("com.ginsberg:junit5-system-exit:${property("systemExit.version")}")
    testImplementation("org.testcontainers:testcontainers:${property("testcontainers.version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junitJupiter.version")}")

}

/* ******************** tests ******************** */

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

tasks.forbiddenApisMain {
    exclude("**/LoggingBootstrap.class")
}

forbiddenApis {
    bundledSignatures = setOf("jdk-deprecated", "jdk-non-portable", "jdk-reflection")
    ignoreFailures = false
}

tasks.forbiddenApisTest { enabled = false }

/* ******************** graal ******************** */

graal {
    graalVersion("19.2.1")
    outputName(rootProject.name)
    mainClass(application.mainClass.get())
    option("-H:+PrintClassInitialization")
    option("-H:ReflectionConfigurationFiles=tools/reflection.json")
    option("-H:-UseServiceLoaderFeature")
    option("-H:IncludeResources=\"org/jline/utils/*.*")
    option("-H:IncludeResources=\"org/jline/terminal/*.*")
    option("--allow-incomplete-classpath")
    option("--report-unsupported-elements-at-runtime")
    option("--initialize-at-build-time")
    option("--initialize-at-run-time=" +
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

/* ******************** Homebrew Package & Formula ******************** */

val buildPackageBrew by tasks.registering(Zip::class) {
    dependsOn(tasks.shadowJar)

    archiveFileName.set(brewZipName)
    destinationDirectory.set(file(buildBrewDir))

    into("brew") {
        from(tasks.shadowJar.get().archiveFile.get())
        from("$brewDir/mqtt")
    }

    from(projectDir) {
        include("LICENSE")
        into("licenses")
    }
}

val buildBrewFormula by tasks.registering(Copy::class) {
    dependsOn(buildPackageBrew)

    from("$brewDir/mqtt-cli.rb")
    into(buildBrewDir)

    doLast {
        val homebrewFile : File = file("$buildBrewDir/mqtt-cli.rb")
        var text : String = homebrewFile.readText()
        text = text.replace("@@description@@", project.description!!)
        text = text.replace("@@version@@", project.version.toString())
        text = text.replace("@@filename@@", buildPackageBrew.get().archiveFileName.get())
        text = text.replace("@@shasum@@", sha256Hash(buildPackageBrew.get().archiveFile.get().asFile))
        homebrewFile.writeText(text)
    }
}

/* ******************** debian and rpm packages ******************** */

ospackage {
    packageName = readableName
    version = project.version.toString()

    url = website

    summary = "MQTT Command Line Interface for interacting with a MQTT broker"
    packageDescription = description
    license = "apache2"
    packager = ""
    vendor = vendor

    os = Os.LINUX
    type = RpmType.BINARY

    user = "root"
    permissionGroup = "root"

    into("/opt/$packageName")
    from(tasks.shadowJar.get().outputs.files)

    from(configurations.runtime.get(), closureOf<CopySpec> {
        into("lib")
    })
    from("lib", closureOf<CopySpec> {
        into("lib")
    })
    from(projectDir, closureOf<CopySpec> {
        include("LICENSE")
        into("licenses")
        fileType = Directive.LICENSE
    })
    from(debDir, closureOf<CopySpec> {
        include("mqtt")
        fileMode = 0b111101101 // 0755
        filter {
            it.replace("@@jarPath@@", "/opt/${packageName}/${tasks.shadowJar.get().archiveFileName.get()}")
        }
    })

    link("/usr/bin/mqtt", "/opt/$packageName/mqtt", 0b111101101)
}

tasks.buildDeb {
    requires("default-jre").or("java8-runtime")
}

tasks.buildRpm {
    setArch(Architecture.NOARCH)
    release = "1"
    requires("jre", "1.8.0", Flags.GREATER or Flags.EQUAL)
}

val buildDebianPackage by tasks.registering(Copy::class){
    from(tasks.buildDeb)
    include("*.deb")
    into(file(buildDebDir))
    rename { fileName: String ->
        fileName.replace(".+".toRegex(), debPackageName)
    }
}

val buildRpmPackage by tasks.registering(Copy::class) {
    from(tasks.buildRpm)
    include("*.rpm")
    into(file(buildRpmDir))
    rename { fileName: String ->
        fileName.replace(".+".toRegex(), rpmPackageName)
    }
}

/* ******************** windows zip ******************** */

launch4j {
    outputDir = "packages/windows"
    headerType = "console"
    mainClassName = application.mainClass.get()
    icon = hmqIco
    jar = "lib/${project.tasks.shadowJar.get().archiveFileName.get()}"
    outfile = appExe
    copyright = copyright
    companyName = vendor
    downloadUrl = "https://openjdk.java.net/install/"
    jreMinVersion = "1.8"
    windowTitle = appName
    version = project.version.toString()
    textVersion = project.version.toString()
}

val buildWindowsZip by tasks.registering(Zip::class) {
    dependsOn(tasks.createExe)

    archiveFileName.set(windowsZipName)
    destinationDirectory.set(file(buildWinDir))

    from(winDir) {
        //include("*")
        filter { line ->
            line.replace("@@exeName@@", appExe)
        }
    }
    from(launch4j.dest)
    from(license)
}

/* ******************** package task ******************** */

val buildPackageAll = tasks.create("buildPackageAll") {
    dependsOn(buildBrewFormula, buildDebianPackage, buildRpmPackage, buildWindowsZip)
}


/* ******************** Publish Draft-Release with all packages to GitHub Releases ******************** */

githubRelease {
    token(System.getenv("githubToken"))
    owner(githubOrg)
    targetCommitish("master")
    body("")
    draft(true)
    prerelease(false)
    releaseAssets
    releaseAssets(tasks.jar.get().archiveFile,
            file("$buildRpmDir/$rpmPackageName"),
            file("$buildDebDir/$debPackageName"),
            file("$buildBrewDir/$brewZipName"),
            buildWindowsZip
    )
    allowUploadToExisting(true)
}

/* ******************** Update the Homebrew-Formula with the newly built package ******************** */

gitPublish {
    repoUri.set("https://github.com/hivemq/homebrew-mqtt-cli.git")
    branch.set("master")

    contents {
        from(buildBrewDir) {
            include("mqtt-cli.rb")
        }
    }

    // message used when committing changes
    commitMessage.set("Release version v${project.version}")
}


tasks.githubRelease {
    dependsOn(buildPackageAll)
}

/* ******************** Dockerhub release ******************** */

jib {
    from {
        image = "openjdk:11-jre-slim-buster"
    }
    to {
        image = "hivemq/mqtt-cli"
        setTags(listOf(project.version.toString()))
        auth {
            username = requireNonNullElse(System.getenv("DOCKER_USER"), "")
            password = requireNonNullElse(System.getenv("DOCKER_PASSWORD"), "")
        }
    }
}


/* ******************** Platform distribution ******************** */


distributions.main {
    shadow {
        distributionBaseName.set("mqtt-cli")
        contents {
            from("README.txt")
            from("build/packaging")
        }
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
            include { Pattern.compile(".*${version}-OpenAPI-spec.yaml").matcher(it.name).matches() }
        }
        into("specs")
    }
}

if (gradle.includedBuilds.find { it.name == "hivemq-swarm" } != null) {
    tasks.register<Copy>("copyHiveMQSwarmSpec") {
        dependsOn(gradle.includedBuild("hivemq-swarm").task(":openApiSpec"))
        mustRunAfter(deleteOldSpecs)

        from("../hivemq-swarm/build/openapi") {
            include { Pattern.compile(".*${version}-OpenAPI-spec.yaml").matcher(it.name).matches() }
        }
        into("specs")
    }
}

if (gradle.includedBuilds.find { it.name == "hivemq-swarm" } != null &&
        gradle.includedBuilds.find { it.name == "hivemq-enterprise" } != null) {
    tasks.register("updateSpecs") {
        dependsOn(deleteOldSpecs)
        dependsOn(tasks.getByName("copyHiveMQSpec"))
        dependsOn(tasks.getByName("copyHiveMQSwarmSpec"))

        doLast {
            val gradleProperties = file("${project.projectDir}/gradle.properties")
            var text = gradleProperties.readText()

            text = text.replace("(?m)^hivemq-api\\.version=.+", "hivemq-api.version=${version}")
            text = text.replace("(?m)^hivemq-swarm-api\\.version=.+", "hivemq-swarm-api.version=${version}")

            gradleProperties.writeText(text)
        }
    }
}

if (gradle.includedBuilds.find { it.name == "plugins" } != null) {
    apply(plugin = "com.hivemq.version-updater")
    project.ext.set("versionUpdaterFiles", arrayOf("doc/docs/installation.md"))
}

/* ******************** Helpers ******************** */

fun sha256Hash(file: File): String {
    val bytes = file.inputStream().readAllBytes()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}
