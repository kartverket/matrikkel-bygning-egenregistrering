package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import models.Bygning
import no.kartverket.matrikkel.bygning.services.BygningService
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService
import org.koin.ktor.ext.inject

fun Route.bygningRouting() {
    route("bygninger") {
        route("{bygningId}") {
            val bygningService by inject<BygningService>()
            val egenregistreringsService by inject<EgenregistreringsService>()

            egenregistreringRouting(egenregistreringsService)

            bygningDoc()
            get {
                val gyldigFra = call.request.queryParameters["gyldigFra"]
                val bygningId = call.parameters["bygningId"]

                if (bygningId == null) {
                    call.respondText("Du må sende med bygningId som parameter", status = HttpStatusCode.BadRequest)
                    return@get
                }

                val gyldigFraDate = gyldigFra?.let {
                    try {
                        LocalDate.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                }

                val bygning = bygningService.getBygning(bygningId.toLong(), gyldigFraDate)

                if (bygning != null) {
                    call.respond(bygning)
                } else {
                    call.respondText("Fant ingen bygninger med id $bygningId", status = HttpStatusCode.NotFound)
                }

            }
        }
    }
}

private fun Route.bygningDoc() {
    install(NotarizedRoute()) {
        parameters = listOf(
            Parameter(
                name = "bygningId", `in` = Parameter.Location.path, schema = TypeDefinition.STRING
            )
        )

        get = GetInfo.builder {
            summary("Hent en bygning")
            description("Henter en bygning med gitte egenregistreringer. Kan også være med en gitt gyldigFra dato.")

            parameters(
                Parameter(
                    name = "gyldigFra",
                    `in` = Parameter.Location.query,
                    schema = TypeDefinition.STRING,
                    required = false
                )
            )

            response {
                responseCode(HttpStatusCode.OK)
                responseType<Bygning>()
                description("Bygning med tilhørende egenregistreringer")
            }

            canRespond {
                responseCode(HttpStatusCode.NotFound)
                responseType<String>()
                description("Fant ikke bygning med gitt id")
            }
        }
    }
}