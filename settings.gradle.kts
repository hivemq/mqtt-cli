pluginManagement {
    includeBuild("mqtt-cli-plugins")

    if (file("../hivemq/plugins").exists()) {
        includeBuild("../hivemq/plugins")
    }
}

rootProject.name = "mqtt-cli"
// PLT-1261 validation: build the OCI plugin from the forked gradle-oci sibling (ECR Public
// blob-HEAD fix, SgtSilvio/gradle-oci#154) when present. Revert before merge.
if (file("../gradle-oci").exists()) {
    includeBuild("../gradle-oci")
}
