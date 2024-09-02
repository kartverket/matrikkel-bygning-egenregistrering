package no.kartverket.matrikkel.bygning.plugins

import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.info.Info
import io.ktor.server.application.*
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.typeOf

fun Application.configureOpenAPI() {
    install(NotarizedApplication()) {
        spec = OpenApiSpec(
            jsonSchemaDialect = "https://spec.openapis.org/oas/3.1/dialect/base",
            info = Info(
                title = "API For Egenregistrering av Bygningsdata",
                version = "0.1",
            ),
        )
        customTypes = mapOf(
            typeOf<LocalDate>() to TypeDefinition(type = "string", format = "date"),
            typeOf<Instant>() to TypeDefinition(type = "string", format = "date-time"),
        )
    }
}
