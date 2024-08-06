package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService

fun Route.bygningRouting(
    bygningClient: BygningClient,
    egenregistreringsService: EgenregistreringsService
) {
    route("bygninger") {
        route("{bygningId}") {
            egenregistreringRouting(bygningClient, egenregistreringsService)

            bygningDoc()
            get {
                val bygningId = call.parameters["bygningId"]

                if (bygningId == null) {
                    call.respondText("Du må sende med bygningId som parameter", status = HttpStatusCode.BadRequest)
                    return@get
                }


                val bygning = bygningClient.getBygningById(bygningId.toLong())

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
            description("Henter en bygning med tilhørende bruksenheter.")

            response {
                responseCode(HttpStatusCode.OK)
                responseType<Bygning>()
                description("Bygning med tilhørende bruksenheter")
            }

            canRespond {
                responseCode(HttpStatusCode.NotFound)
                responseType<String>()
                description("Fant ikke bygning med gitt id")
            }
        }
    }
}
