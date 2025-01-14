package no.kartverket.matrikkel.bygning.plugins

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.AuthScheme
import io.github.smiley4.ktorswaggerui.data.AuthType
import io.github.smiley4.ktorswaggerui.dsl.config.PluginConfigDsl
import io.ktor.server.application.*

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
