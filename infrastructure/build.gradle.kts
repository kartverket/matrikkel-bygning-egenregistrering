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

    // Persistence
    implementation(libs.postgres)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.hikari)

    // Serialization
    implementation(libs.ktor.serialization.kotlinx)

    // Matrikkel
    implementation(libs.kartverket.ws.client)
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
    }
}
