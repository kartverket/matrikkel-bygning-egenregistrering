
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
    kotlin("jvm").version(libs.versions.kotlinVersion)
    kotlin("plugin.serialization").version(libs.versions.kotlinVersion)

    alias(libs.plugins.ktor)
    alias(libs.plugins.shadow)

    `jvm-test-suite`
}

allprojects {
    group = "no.kartverket.matrikkel.bygning"
    version = "0.0.1"
}

application {
    mainClass.set("no.kartverket.matrikkel.bygning.ApplicationKt")
}

dependencies {
    implementation(project(":matrikkel-api"))

    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)

    // Monitoring
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.prometheus)

    // Logs
    implementation(libs.logstash.encoder)
    implementation(libs.logback.classic)
    implementation(libs.jackson.databind)

    // Serialization
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx)

    // Persistence
    implementation(libs.postgres)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.hikari)

    // OpenAPI
    implementation(libs.kompendium.core)
}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        archiveBaseName.set("${project.name}-all")
    }
}

tasks.withType<Test> {
    testLogging {
        showCauses = true
        showExceptions = true
        exceptionFormat = TestExceptionFormat.FULL
        events(PASSED, SKIPPED, FAILED)
    }
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        withType<JvmTestSuite> {
            useJUnitJupiter()
        }

        named<JvmTestSuite>("test") {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.assertk)
                implementation(libs.mockk)
                implementation(testFixtures(project(":matrikkel-api")))
            }
        }

        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(project())

                implementation(libs.ktor.serialization.kotlinx)

                implementation(libs.kotlin.test)
                implementation(libs.ktor.server.tests)
                implementation(libs.ktor.client.content.negotation)

                implementation(libs.assertk)
                implementation(libs.testcontainers.postgresql)
            }
        }
    }
}
