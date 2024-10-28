import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization").version(libs.versions.kotlinVersion)
    alias(libs.plugins.shadow)
    `jvm-test-suite`
}

dependencies {
    implementation(project(":application"))
    implementation(project(":infrastructure"))

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
    implementation(libs.logback.classic)
    runtimeOnly(libs.logstash.encoder)

    // Serialization
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx)

    // OpenAPI
    implementation ("io.github.smiley4:ktor-swagger-ui:4.0.0")
}

application {
    mainClass.set("no.kartverket.matrikkel.bygning.ApplicationKt")
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    archiveBaseName.set("app")
    archiveClassifier.set("")
    archiveVersion.set("")
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
            }
        }

        // TODO: Bør repository tester ligge i infrastructure?
        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(project())
                implementation(project(":web"))
                implementation(project(":application"))
                implementation(project(":infrastructure"))

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

