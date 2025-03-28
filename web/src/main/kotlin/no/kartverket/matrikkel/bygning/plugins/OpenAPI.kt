package no.kartverket.matrikkel.bygning.plugins

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.config.OpenApiPluginConfig
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.config.SecurityConfig
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.ENTRA_ID_ARKIVARISK_HISTORIKK_NAME
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.IDPORTEN_PROVIDER_NAME
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MASKINPORTEN_PROVIDER_NAME
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MATRIKKEL_AUTH_BERETTIGET_INTERESSE
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MATRIKKEL_AUTH_MED_PERSONDATA
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MATRIKKEL_AUTH_UTEN_PERSONDATA

object OpenApiSpecIds {
    const val INTERN = "intern"
    const val HENDELSER = "hendelser"
    const val BERETTIGET_INTERESSE = "berettigetinteresse"
    const val UTEN_PERSONDATA = "utenpersondata"
    const val MED_PERSONDATA = "medpersondata"
}

fun Application.configureOpenAPI() {
    install(OpenApi) {
        installOpenApiSpec(
            name = OpenApiSpecIds.INTERN,
            title = "API for egenregistrering av bygningsdata",
            version = "0.1",
        ) {
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
        installOpenApiSpec(
            name = OpenApiSpecIds.HENDELSER,
            title = "API for hendelseslogg for egenregistrerte bygningsdata",
            version = "0.1",
        ) {
            securityScheme(MASKINPORTEN_PROVIDER_NAME) {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "jwt"
            }
        }
        installOpenApiSpec(
            name = OpenApiSpecIds.BERETTIGET_INTERESSE,
            title = "API for utlevering av egenregistrerte bygningsdata",
            version = "0.1",
        ) {
            securityScheme(MASKINPORTEN_PROVIDER_NAME) {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "jwt"
            }

            securityScheme(MATRIKKEL_AUTH_BERETTIGET_INTERESSE) {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "jwt"
            }
        }
        installOpenApiSpec(
            name = OpenApiSpecIds.UTEN_PERSONDATA,
            title = "API for utlevering av egenregistrerte bygningsdata uten personidentifiserende metadata",
            version = "0.1",
        ) {
            securityScheme(MASKINPORTEN_PROVIDER_NAME) {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "jwt"
            }

            securityScheme(MATRIKKEL_AUTH_UTEN_PERSONDATA) {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "jwt"
            }
        }
        installOpenApiSpec(
            name = OpenApiSpecIds.MED_PERSONDATA,
            title = "API for utlevering av egenregistrerte bygningsdata med personidentifiserende metadata",
            version = "0.1",
        ) {
            securityScheme(MASKINPORTEN_PROVIDER_NAME) {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "jwt"
            }

            securityScheme(MATRIKKEL_AUTH_MED_PERSONDATA) {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "jwt"
            }
        }
    }
}

private fun OpenApiPluginConfig.installOpenApiSpec(
    name: String,
    title: String,
    version: String,
    securityConfig: SecurityConfig.() -> Unit,
) {
    spec(name) {
        schemas {
            generator =
                SchemaGenerator.reflection {
                    customGenerator(SchemaGenerator.TypeOverwrites.Instant())
                    explicitNullTypes = false
                    referencePath = RefType.SIMPLE
                }
        }
        postBuild =
            { openApi, _ -> openApi.externalDocs = null } // Ellers peker den på et sted hvor det ikke ligger noe
        info {
            this.title = title
            this.version = version
        }
        tags {
            tagGenerator = { url ->
                listOf(
                    when (url.getOrNull(1)) {
                        "hendelser" -> url.getOrNull(1)?.replaceFirstChar(Char::titlecase)
                        else -> url.getOrNull(2)?.replaceFirstChar(Char::titlecase)
                    },
                )
            }
        }
        security {
            securityConfig()
        }
    }
}
