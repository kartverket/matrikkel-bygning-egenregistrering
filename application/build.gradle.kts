import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization").version(libs.versions.kotlinVersion)

    `jvm-test-suite`
}

dependencies {
    // Error handling
    api(libs.kotlin.result)

    // Serialization
    // TODO: Bør helst ikke måtte ligge i application (skyldes lagring i databasen)
    implementation(libs.ktor.serialization.kotlinx)

    // Norwegian Commons
    implementation(libs.norwegian.commons)
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
