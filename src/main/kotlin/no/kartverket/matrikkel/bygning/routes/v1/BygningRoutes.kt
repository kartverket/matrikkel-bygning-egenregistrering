package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import no.kartverket.matrikkel.bygning.matrikkel.Bygning
import no.kartverket.matrikkel.bygning.matrikkel.BygningClient

fun Route.bygningRouting(
    bygningClient: BygningClient,
) {
    route("{bygningId}") {
        bygningDoc()

        get {
            val bygningId = call.parameters.getOrFail("bygningId").toLong()

            val bygning = bygningClient.getBygningById(bygningId)
                ?: call.respondText("Fant ingen bygninger med id $bygningId", status = HttpStatusCode.NotFound)

            call.respond(bygning)
        }
    }
}

private fun Route.bygningDoc() {
    install(NotarizedRoute()) {
        tags = setOf("Bygninger")
        parameters = listOf(
            Parameter(
                name = "bygningId", `in` = Parameter.Location.path, schema = TypeDefinition.STRING,
            ),
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
