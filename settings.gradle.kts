plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "matrikkel-bygning-egenregistrering"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":matrikkel-api")
include(":infrastructure")
include(":application")
include(":web")
