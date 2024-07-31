package no.kartverket.matrikkel.bygning.gradle.wsimport

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.withGroovyBuilder
import javax.inject.Inject

abstract class WsImportTask @Inject constructor(objectFactory: ObjectFactory) : SourceTask() {
    private val classpath = objectFactory.fileCollection()

    @Classpath
    fun getClasspath(): FileCollection = classpath

    fun setClasspath(classpath: FileCollection) {
        // DefaultJavaExecSpec lager også ny this.classpath. Usikker på hva som er forskjellen på from og setFrom.
        this.classpath.setFrom(classpath)
    }

    fun classpath(vararg paths: Any) {
        classpath.from(paths)
    }

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun wsimport() {
        ant.withGroovyBuilder {
            "delete"("includeEmptyDirs" to "true") {
                "fileset"("dir" to outputDirectory.get(), "includes" to "**/*")
            }
        }
        source.matching { include("**/*.wsdl") }
            .visit {
                if (!this.isDirectory) {
                    val fullPath = this.file.path
                    val relPath = this.path
                    this@WsImportTask.logger.info(relPath)
                    project.javaexec {
                        classpath = this@WsImportTask.classpath
                        mainClass.set("com.sun.tools.ws.WsImport")
                        args("-s", outputDirectory.get().asFile)
                        args("-wsdllocation", "/$relPath")
                        args("-keep", "-Xnocompile", "-quiet")
                        args(fullPath)
                    }.assertNormalExitValue()
                }
            }
    }
}
