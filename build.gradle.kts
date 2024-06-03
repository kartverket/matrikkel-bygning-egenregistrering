import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val prometheus_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("io.ktor.plugin") version "2.3.11"

    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "no.kartverket.matrikkel.bygning"
version = "0.0.1"

application {
    mainClass.set("no.kartverket.matrikkel.bygning.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")

}

repositories {
    mavenCentral()
}



dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-swagger:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-openapi:$ktor_version")

    // Monitoring
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheus_version")

    // Logs
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")

    // Serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")

    // Persistence
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.flywaydb:flyway-core:10.13.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.14.0")

    // Tests
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

ktor {
    fatJar {
        archiveFileName.set("matrikkel-bygning-egenregistering.jar")
    }
}


tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        archiveBaseName.set("${project.name}-all")
    }
}