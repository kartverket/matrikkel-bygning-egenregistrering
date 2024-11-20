package no.kartverket.matrikkel.bygning.plugins

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.handleSwaggerAnnotations
import io.github.smiley4.schemakenerator.swagger.withTitle
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
        schemas {
            generator = { type ->
                type
                    .processReflection()
                    .generateSwaggerSchema()
                    .withTitle(TitleType.SIMPLE)
                    .handleCoreAnnotations()
                    .handleSwaggerAnnotations()
                    .compileReferencingRoot()
            }
        }
    }
}

