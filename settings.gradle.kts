rootProject.name = "mqtt-cli"

if (file("../plugins").exists()) {
    includeBuild("../plugins")
}
if (file("../hivemq-enterprise").exists()) {
    includeBuild("../hivemq-enterprise")
}
if (file("../hivemq-swarm").exists()) {
    includeBuild("../hivemq-swarm")
}