plugins {
    `kotlin-dsl`
}

group = "com.hivemq"

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("native-image") {
            id = "$group.cli.$name"
            implementationClass = "$group.cli.native_image.CliNativeImagePlugin"
        }
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
