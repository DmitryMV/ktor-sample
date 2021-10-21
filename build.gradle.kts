val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val prometeus_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.31"
    id("org.sonarqube") version "3.3"
    jacoco
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

var sonarToken: String? = project.findProperty("sonarToken") as String? ?: System.getenv("SONAR_TOKEN")

jacoco {
    toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

tasks.sonarqube {
    enabled = sonarToken != null
}

sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "dmitrymv-playground")
        property("sonar.projectKey", "dmitrymv-playground_ktor-sample")

        property("sonar.login", sonarToken!!)

        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            jacoco.reportsDirectory.file("test/jacocoTestReport.xml").get()
        )
        property(
            "sonar.junit.reportPaths",
            (tasks["test"] as Test).reports.junitXml.outputLocation.get()
        )
        if ("true" == System.getenv("PR_BUILD")) {
            property(
                "sonar.pullrequest.key", System.getenv("GITHUB_PR_KEY")
            )
            property(
                "sonar.pullrequest.branch", System.getenv("GITHUB_PR_HEAD")
            )
            property(
                "sonar.pullrequest.base", System.getenv("GITHUB_PR_BASE")
            )
            property(
                "sonar.pullrequest.github.repository", "dmitrymv-playground/ktor-sample"
            )
            property(
                "sonar.pullrequest.github.endpoint", "https://api.github.com"
            )
            property("sonar.pullrequest.provider", "GitHub")
        } else {
            property(
                "sonar.branch.name", System.getenv("SONAR_BRANCH_NAME")
            )
        }
        property(
            "sonar.links.homepage", System.getenv("GITHUB_REPO_URL")
        )
        property("sonar.scm.provider", "git")
        property(
            "sonar.scm.revision", System.getenv("GITHUB_PR_SHA")
        )
    }
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
