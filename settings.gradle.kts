pluginManagement {
    includeBuild("mqtt-cli-plugins")

    if (file("../hivemq/plugins").exists()) {
        includeBuild("../hivemq/plugins")
    }
}

rootProject.name = "mqtt-cli"
