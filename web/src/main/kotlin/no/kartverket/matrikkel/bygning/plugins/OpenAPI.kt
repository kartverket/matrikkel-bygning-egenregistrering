package no.kartverket.matrikkel.bygning.plugins

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.AuthScheme
import io.github.smiley4.ktorswaggerui.data.AuthType
import io.github.smiley4.ktorswaggerui.dsl.config.PluginConfigDsl
import io.github.smiley4.schemakenerator.core.connectSubTypes
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.handleNameAnnotation
import io.github.smiley4.schemakenerator.reflection.ReflectionTypeProcessingStepConfig
import io.github.smiley4.schemakenerator.reflection.collectSubTypes
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.customizeTypes
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.ktor.server.application.*
import java.time.Instant

object OpenApiSpecIds {
    const val INTERN = "intern"
    const val EKSTERN = "ekstern"
}

fun Application.configureOpenAPI() {
    install(SwaggerUI) {
        installOpenApiSpec(
            name = OpenApiSpecIds.INTERN,
            title = "API For Egenregistrering av Bygningsdata",
            version = "0.1",
        )
        installOpenApiSpec(
            name = OpenApiSpecIds.EKSTERN,
            title = "API For uthenting av Egenregistreringsdata for eksterne",
            version = "0.1",
        )
    }
}

private fun PluginConfigDsl.installOpenApiSpec(name: String, title: String, version: String) {
    spec(name) {
        schemas {
            generator = { type ->
                type
                    .collectSubTypes()
                    .processReflection(registerCustomProcessors())
                    .connectSubTypes()
                    .handleNameAnnotation()
                    .generateSwaggerSchema()
                    .handleCoreAnnotations()
                    .customizeTypes { typeData, typeSchema ->
                        // Prosesserer alle typer som har fått lagt på en custom annotasjon
                        typeData.annotations.find { it.name == "type_format_annotation" }?.also { annotation ->
                            typeSchema.format = annotation.values["format"]?.toString()
                            typeSchema.types = setOf(annotation.values["type"]?.toString())
                        }
                    }
                    .withTitle(TitleType.SIMPLE)
                    .compileReferencingRoot()
            }
        }
        info {
            this.title = title
            this.version = version
        }
        tags {
            tagGenerator = { url -> listOf(url.getOrNull(1)?.replaceFirstChar(Char::titlecase)) }
        }
        security {
            securityScheme("Maskinporten") {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "jwt"
            }
        }
    }
}

private fun registerCustomProcessors(): ReflectionTypeProcessingStepConfig.() -> Unit = {
    customProcessor<Instant> {
        createDefaultPrimitiveTypeData<Instant>(
            type = "string",
            format = "date-time",
        )
    }
}

/**
 * Legger på en custom annotasjon som inneholder type og format som senere blir prosessert og gjør at type og format blir satt korret på swagger schema
 * Bør kunne løses gjennom annotasjoner, men avhengig av et issue som må løses: https://github.com/SMILEY4/schema-kenerator/issues/42
 */
private inline fun <reified T> createDefaultPrimitiveTypeData(type: String, format: String): PrimitiveTypeData {
    return PrimitiveTypeData(
        id = TypeId.build(T::class.qualifiedName!!),
        simpleName = T::class.simpleName!!,
        qualifiedName = T::class.qualifiedName!!,
        annotations = mutableListOf(
            AnnotationData(
                name = "type_format_annotation",
                values = mutableMapOf(
                    "type" to type,
                    "format" to format,
                ),
                annotation = null,
            ),
        ),
    )
}
