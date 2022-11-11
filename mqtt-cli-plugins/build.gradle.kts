plugins {
    `kotlin-dsl`
}

group = "com.hivemq"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(gradleApi())
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
