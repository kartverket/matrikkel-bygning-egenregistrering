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

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")

}

dependencies {
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

    // Tests
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test)

    implementation(project(":matrikkel-bygning-matrikkel-api"))
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
