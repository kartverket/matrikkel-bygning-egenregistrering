package no.kartverket.matrikkel.bygning.plugins

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.ktor.server.application.*

fun Application.configureOpenAPI() {
    install(SwaggerUI) {
        info {
            title = "API For Egenregistrering av Bygningsdata"
            version = "0.1"
        }
        tags {
            tagGenerator = { url -> listOf(url.getOrNull(1)?.replaceFirstChar(Char::titlecase)) }
        }
    }
}
