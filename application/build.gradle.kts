import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
    kotlin("jvm")
    `jvm-test-suite`
}

dependencies {
    // Error handling
    api(libs.kotlin.result)

    // Serialization
    implementation(libs.fasterxml.jackson.kotlin)

    // Norwegian Commons
    implementation(libs.norwegian.commons)

    implementation(libs.kotli.query) {
        exclude(group = "joda-time")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add(
            // https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-consistent-copy-visibility/
            "-Xconsistent-data-class-copy-visibility",
        )
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
    }
}
