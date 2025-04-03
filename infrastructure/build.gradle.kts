import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
    kotlin("jvm")
    `jvm-test-suite`
}

dependencies {
    api(project(":application"))

    implementation(libs.logback.classic)
    implementation(libs.kotlinx.coroutines.core)

    // Persistence
    implementation(libs.postgres)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.hikari)
    implementation(libs.kotli.query) {
        // Ekskluderer joda-time da vi ikke benytter dette i sql mappingen vår
        exclude(group = "joda-time")
    }
    implementation(libs.ktor.client.content.negotation)
    implementation(libs.ktor.serialization.kotlinx)

    implementation(libs.ktor.client.core.jvm)
    implementation(libs.ktor.client.okhttp)

    // Serialization
    implementation(libs.fasterxml.jackson.kotlin)

    // Matrikkel
    implementation(libs.matrikkelapi.ws.client)
    runtimeOnly(libs.jaxws.rt) {
        exclude(group = "org.eclipse.angus") // Ekskluderer angus email
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
            }
        }

        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(project())
                implementation(project(":application"))

                implementation(libs.kotli.query)

                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)

                implementation(libs.assertk)
                implementation(libs.testcontainers.postgresql)
                implementation(libs.wiremock)
            }
        }
    }
}
