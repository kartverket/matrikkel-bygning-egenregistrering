plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}
rootProject.name = "matrikkel-bygning-egenregistrering"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":matrikkel-api")
include(":application")
include(":infrastructure")
include(":web")
