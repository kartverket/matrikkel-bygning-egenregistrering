import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm").version(libs.versions.kotlinVersion)
    kotlin("plugin.serialization").version(libs.versions.kotlinVersion)

    alias(libs.plugins.ktor)
    alias(libs.plugins.shadow)
}

allprojects {
    group = "no.kartverket.matrikkel.bygning"
    version = "0.0.1"
}

application {
    mainClass.set("no.kartverket.matrikkel.bygning.ApplicationKt")
}

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val intTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

dependencies {
    implementation(project(":matrikkel-api"))

    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.openapi)

    // Monitoring
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.prometheus)

    // Logs
    implementation(libs.logstash.encoder)
    implementation(libs.logback.classic)
    implementation(libs.jackson.databind)

    // Serialization
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx)
    implementation(libs.kotlinx.datetime)

    // Persistence
    implementation(libs.postgres)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.hikari)

    implementation(libs.kompendium.core)

    // Dependency injection
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)

    // Integration tests
    intTestImplementation(libs.kotlin.test)
    intTestImplementation(libs.ktor.server.tests)
    intTestImplementation(libs.ktor.client.content.negotation)

    intTestImplementation(libs.assertj)
    intTestImplementation(libs.testcontainers.postgresql)
}

ktor {
    fatJar {
        archiveFileName.set("matrikkel-bygning-egenregistrering.jar")
    }
}


tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        archiveBaseName.set("${project.name}-all")
    }
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    shouldRunAfter("test")

    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.check { dependsOn(integrationTest) }
