import kotlinx.kover.api.CoverageEngine.INTELLIJ
import kotlinx.kover.api.CoverageEngine.JACOCO
import kotlinx.kover.api.KoverTaskExtension

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val prometeus_version: String by project
val koin_version: String by project
val assertj_version: String by project
val json_unit_version: String by project
val jackson_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.31"
    id("org.sonarqube") version "3.3"
//    jacoco
    id("org.jetbrains.kotlinx.kover") version "0.4.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-metrics-micrometer:$ktor_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")
    implementation("io.ktor:ktor-metrics:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    // Koin
    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    // Misc
    implementation("ch.qos.logback:logback-classic:$logback_version")
    // Testing
    testImplementation("io.insert-koin:koin-test:$koin_version")
    testImplementation("io.insert-koin:koin-test-junit5:$koin_version")
    testImplementation("org.assertj:assertj-core:$assertj_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:$json_unit_version")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jackson_version")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
}

group = "com.example"
version = "0.1.0"

val dockerRegistryRepo = "dmitrymv"
val dockerImageName = "$dockerRegistryRepo/${project.name}"
val dockerImageTag = version
val dockerImage = "$dockerImageName:$dockerImageTag"
val dockerExecutable = "${project.name}-${project.version}/${application.executableDir}/${project.name}"

var sonarToken: String? = project.findProperty("sonarToken") as String? ?: System.getenv("SONAR_TOKEN")

application {
    mainClass.set("com.example.ApplicationKt")
}

java {
    toolchain {
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kover {
    coverageEngine.set(JACOCO)
}

tasks.test {
    extensions.configure(KoverTaskExtension::class) {
        excludes = listOf(
            "com\\.example\\.ApplicationKt.*",
            "com\\.example\\.plugins\\.MonitoringKt.*"
        )
    }
}

sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "dmitrymv-playground")
        property("sonar.projectKey", "dmitrymv-playground_ktor-sample")

        property("sonar.login", sonarToken!!)

        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${project.reporting.baseDir}/kover/report.xml"
        )
        property(
            "sonar.coverage.exclusions",
            "**/com/example/Application.kt, **/com/example/plugins/Monitoring.kt"
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

tasks.sonarqube {
    enabled = sonarToken != null
}

task<Exec>("buildImage") {
    group = "docker"
    description = "Assemble docker image"
    dependsOn("assembleDist")
    commandLine(
        "docker",
        "build",
        "--build-arg",
        "DIST_PATH=${(tasks["distTar"] as Tar).archiveFile.get().asFile.relativeTo(project.projectDir)}",
        "--build-arg",
        "EXECUTABLE=${dockerExecutable}",
        "-t",
        dockerImage,
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
        dockerImage
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
        dockerImage
    )
}
