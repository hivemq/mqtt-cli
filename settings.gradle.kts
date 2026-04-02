pluginManagement {
    includeBuild("mqtt-cli-plugins")

    if (file("../hivemq/plugins").exists()) {
        includeBuild("../hivemq/plugins")
    }
}

plugins {
    id("com.hivemq.tools.oci-version-catalog") version "0.2.0"
}

rootProject.name = "mqtt-cli"
