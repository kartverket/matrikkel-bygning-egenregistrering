import no.kartverket.matrikkel.bygning.gradle.wsimport.WsImportTask

plugins {
    `java-library`
    kotlin("jvm")
    idea
}

val jaxws: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation(libs.jaxws.rt) {
        exclude(group = "org.eclipse.angus") // Ekskluderer angus email
    }
    jaxws(libs.jaxws.tools)
}

val wsImportDir: Provider<Directory> = project.layout.buildDirectory.dir("generated/sources/wsimport")

val wsimport = tasks.register("wsimport", WsImportTask::class) {
    outputDirectory.set(wsImportDir)
    setClasspath(jaxws)
    source(sourceSets.main.get().resources)
    include(
        "matrikkel/StoreServiceWS.wsdl",
        "matrikkel/KommuneServiceWS.wsdl",
        "matrikkel/BygningServiceWS.wsdl",
    )
}

sourceSets {
    main {
        java {
            srcDir(wsimport)
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:-dep-ann") // Wsimport legger til javadoc fra tekst som inneholder @deprecated
}

idea.module {
    generatedSourceDirs = generatedSourceDirs + project.files(wsImportDir)
}
