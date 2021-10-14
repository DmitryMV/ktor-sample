val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val prometeus_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.31"
}

group = "com.example"
version = "0.1.0"

val dockerRegistryRepo = "dmitrymv"
val dockerImageName = project.name
val dockerVersionTag = version

application {
    mainClass.set("com.example.ApplicationKt")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-metrics-micrometer:$ktor_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")
    implementation("io.ktor:ktor-metrics:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
}

task<Exec>("buildImage") {
    group = "docker"
    description = "Assemble docker image"
    dependsOn("assembleDist")
    commandLine(
        "docker",
        "build",
        "-t",
        "$dockerRegistryRepo/$dockerImageName:$dockerVersionTag",
        "."
    )
}

task<Exec>("pushImage") {
    group = "docker"
    description = "Push image to DockerHub"
    dependsOn("buildImage")
    commandLine(
        "docker",
        "push",
        "-t",
        "$dockerRegistryRepo/$dockerImageName:$dockerVersionTag"
    )
}

task<Exec>("runImage") {
    group = "docker"
    description = "Run Image"
    dependsOn("buildImage")
    commandLine(
        "docker",
        "run",
        "-dp",
        "8080:8080",
        "--rm",
        "$dockerRegistryRepo/$dockerImageName:$dockerVersionTag"
    )
}
