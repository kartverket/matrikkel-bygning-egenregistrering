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
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MATRIKKEL_AUTH_VIRKSOMHET_BEGRENSET
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MATRIKKEL_AUTH_VIRKSOMHET_UTVIDET
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MATRIKKEL_AUTH_VIRKSOMHET_UTVIDET_UTEN_PII
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.VIRKSOMHET_BEGRENSET
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.VIRKSOMHET_HENDELSER
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.VIRKSOMHET_UTVIDET
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.VIRKSOMHET_UTVIDET_UTEN_PII

object OpenApiSpecIds {
    const val INTERN = "intern"
    const val HENDELSER = "hendelser"
}

fun Application.configureOpenAPI() {
    install(OpenApi) {
        installOpenApiSpec(
            name = OpenApiSpecIds.INTERN,
            title = "API for egenregistrering av bygningsdata",
            version = "0.1",
        ) {
            jwtSecurityScheme(IDPORTEN_PROVIDER_NAME)
            jwtSecurityScheme(ENTRA_ID_ARKIVARISK_HISTORIKK_NAME)
        }
        installOpenApiSpec(
            name = OpenApiSpecIds.HENDELSER,
            title = "API for hendelseslogg for egenregistrerte bygningsdata",
            version = "0.1",
        ) {
            jwtSecurityScheme(VIRKSOMHET_HENDELSER.maskinportenAuthSchemeName)
        }
        installOpenApiSpec(
            name = VIRKSOMHET_BEGRENSET.openApiSpecId,
            title = "API for utlevering av egenregistrerte bygningsdata",
            version = "0.1",
        ) {
            jwtSecurityScheme(VIRKSOMHET_BEGRENSET.maskinportenAuthSchemeName)
            jwtSecurityScheme(MATRIKKEL_AUTH_VIRKSOMHET_BEGRENSET)
        }
        installOpenApiSpec(
            name = VIRKSOMHET_UTVIDET_UTEN_PII.openApiSpecId,
            title = "API for utlevering av egenregistrerte bygningsdata uten personidentifiserende informasjon",
            version = "0.1",
        ) {
            jwtSecurityScheme(VIRKSOMHET_UTVIDET_UTEN_PII.maskinportenAuthSchemeName)
            jwtSecurityScheme(MATRIKKEL_AUTH_VIRKSOMHET_UTVIDET_UTEN_PII)
        }
        installOpenApiSpec(
            name = VIRKSOMHET_UTVIDET.openApiSpecId,
            title = "API for utlevering av egenregistrerte bygningsdata",
            version = "0.1",
        ) {
            jwtSecurityScheme(VIRKSOMHET_UTVIDET.maskinportenAuthSchemeName)
            jwtSecurityScheme(MATRIKKEL_AUTH_VIRKSOMHET_UTVIDET)
        }
    }
}

private fun SecurityConfig.jwtSecurityScheme(name: String) {
    securityScheme(name) {
        type = AuthType.HTTP
        scheme = AuthScheme.BEARER
        bearerFormat = "jwt"
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
