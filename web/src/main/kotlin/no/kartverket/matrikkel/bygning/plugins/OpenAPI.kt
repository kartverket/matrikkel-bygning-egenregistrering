package no.kartverket.matrikkel.bygning.plugins

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.AuthScheme
import io.github.smiley4.ktorswaggerui.data.AuthType
import io.github.smiley4.ktorswaggerui.dsl.config.PluginConfigDsl
import io.github.smiley4.schemakenerator.core.annotations.Format
import io.github.smiley4.schemakenerator.core.annotations.Type
import io.github.smiley4.schemakenerator.core.connectSubTypes
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.handleNameAnnotation
import io.github.smiley4.schemakenerator.reflection.collectSubTypes
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.customizeProperties
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.handleSchemaAnnotations
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.ktor.server.application.*
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.ENTRA_ID_ARKIVARISK_HISTORIKK_NAME
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.IDPORTEN_PROVIDER_NAME
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MASKINPORTEN_PROVIDER_NAME
import java.time.Instant

object OpenApiSpecIds {
    const val INTERN = "intern"
    const val HENDELSER = "hendelser"
    const val MED_PERSONDATA = "medpersondata"
}

fun Application.configureOpenAPI() {
    install(SwaggerUI) {
        installOpenApiSpec(
            name = OpenApiSpecIds.INTERN,
            title = "API for egenregistrering av bygningsdata",
            version = "0.1",
        )
        installOpenApiSpec(
            name = OpenApiSpecIds.HENDELSER,
            title = "API for hendelseslogg for egenregistrerte bygningsdata",
            version = "0.1",
        )
        installOpenApiSpec(
            name = OpenApiSpecIds.MED_PERSONDATA,
            title = "API for utlevering av egenregistrerte bygningsdata med personidentifiserende data",
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
                    .processReflection {
                        customProcessor<Instant> {
                            createDefaultPrimitiveTypeWithCustomTypeAndFormat<Instant>(
                                type = "string",
                                format = "date-time",
                            )
                        }
                    }
                    .connectSubTypes()
                    .handleNameAnnotation()
                    .generateSwaggerSchema()
                    // Setter nullable properties til ikke nullable i Swagger skjemaet
                    // Gjør at vi unngår oneOf(null, type) i generert Swagger skjema.
                    .customizeProperties { propertyData, propertySchema ->
                        if (propertyData.nullable) {
                            propertySchema.nullable = false
                        }
                    }
                    .handleCoreAnnotations()
                    .handleSchemaAnnotations()
                    .withTitle(TitleType.SIMPLE)
                    .compileReferencingRoot()
            }
        }
        info {
            this.title = title
            this.version = version
        }
        tags {
            tagGenerator = { url ->
                listOf(
                    when (url.getOrNull(1)) {
                        "intern" -> url.getOrNull(2)?.replaceFirstChar(Char::titlecase)
                        else -> url.getOrNull(1)?.replaceFirstChar(Char::titlecase)
                    }
                )
            }
        }
        security {
            securityScheme(MASKINPORTEN_PROVIDER_NAME) {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "jwt"
            }

            securityScheme(IDPORTEN_PROVIDER_NAME) {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "jwt"
            }

            securityScheme(ENTRA_ID_ARKIVARISK_HISTORIKK_NAME) {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "jwt"
            }
        }
    }
}

/**
 * Legger på en type- og format-annotasjoner gjør at type og format blir satt korret på swagger schema
 * Siden Instant er en ekstern-klasse må vi sette disse som en del av prosessering dynamisk i stedet for å sette annotasjonene direkte på klassen
 */
private inline fun <reified T> createDefaultPrimitiveTypeWithCustomTypeAndFormat(type: String, format: String): PrimitiveTypeData {
    return PrimitiveTypeData(
        id = TypeId.build(T::class.qualifiedName!!),
        simpleName = T::class.simpleName!!,
        qualifiedName = T::class.qualifiedName!!,
        annotations = mutableListOf(
            AnnotationData(
                name = Type::class.qualifiedName!!,
                values = mutableMapOf("type" to type),
            ),
            AnnotationData(
                name = Format::class.qualifiedName!!,
                values = mutableMapOf("format" to format),
            ),
        ),
    )
}
