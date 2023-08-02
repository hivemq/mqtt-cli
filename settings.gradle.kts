rootProject.name = "mqtt-cli"

pluginManagement {
    plugins {
        id("com.github.johnrengelman.shadow") version "${extra["plugin.shadow.version"]}"
        id("io.github.sgtsilvio.gradle.defaults") version "${extra["plugin.defaults.version"]}"
        id("nebula.ospackage") version "${extra["plugin.ospackage.version"]}"
        id("edu.sc.seis.launch4j") version "${extra["plugin.launch4j.version"]}"
        id("org.openapi.generator") version "${extra["plugin.openapi.generator.version"]}"
        id("com.google.cloud.tools.jib") version "${extra["plugin.jib.version"]}"
        id("com.github.hierynomus.license") version "${extra["plugin.license.version"]}"
        id("de.thetaphi.forbiddenapis") version "${extra["plugin.forbiddenapis.version"]}"
        id("com.github.breadmoirai.github-release") version "${extra["plugin.github-release.version"]}"
        id("org.ajoberstar.git-publish") version "${extra["plugin.git-publish.version"]}"
        id("org.graalvm.buildtools.native") version "${extra["plugin.graal.version"]}"
    }
}

includeBuild("mqtt-cli-plugins")

if (file("../hivemq/plugins").exists()) {
    includeBuild("../hivemq/plugins")
}
