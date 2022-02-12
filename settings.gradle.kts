rootProject.name = "mqtt-cli"

pluginManagement {
    plugins {
        id("com.github.johnrengelman.shadow") version "${extra["plugin.shadow.version"]}"
        id("com.github.hierynomus.license") version "${extra["plugin.license.version"]}"
        id("nebula.ospackage") version "${extra["plugin.ospackage.version"]}"
        id("edu.sc.seis.launch4j") version "${extra["plugin.launch4j.version"]}"
        id("com.palantir.graal") version "${extra["plugin.graal.version"]}"
        id("de.thetaphi.forbiddenapis") version "${extra["plugin.forbiddenapis.version"]}"
        id("com.github.breadmoirai.github-release") version "${extra["plugin.github-release.version"]}"
        id("org.ajoberstar.git-publish") version "${extra["plugin.git-publish.version"]}"
        id("org.owasp.dependencycheck") version "${extra["plugin.dependencycheck.version"]}"
        id("com.github.ben-manes.versions") version "${extra["plugin.ben-manes.versions.version"]}"
        id("org.openapi.generator") version "${extra["plugin.openapi.generator.version"]}"
        id("com.google.cloud.tools.jib") version "${extra["plugin.jib.version"]}"
    }

    if (file("../hivemq/plugins").exists()) {
        includeBuild("../hivemq/plugins")
    }
}

if (file("../hivemq-enterprise").exists()) {
    includeBuild("../hivemq-enterprise")
}
if (file("../hivemq-swarm").exists()) {
    includeBuild("../hivemq-swarm")
}
