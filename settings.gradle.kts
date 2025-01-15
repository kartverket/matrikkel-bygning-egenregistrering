plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}
rootProject.name = "matrikkel-bygning-egenregistrering"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            name = "WSClient"
            url = uri("https://maven.pkg.github.com/kartverket/matrikkelapi-v1-wsclient")

            val token = System.getenv("KV_PACKAGES_PAT") // For GitHub workflows
                ?: extra["KV_PACKAGES_PAT"] // For local development
                ?: throw InvalidUserDataException("missing 'KV_PACKAGES_PAT' property")
            val tokenString = token as String

            credentials {
                username = "x-access-token" // Dummy username
                password = tokenString
            }
        }
    }
}

include(":application")
include(":infrastructure")
include(":web")
