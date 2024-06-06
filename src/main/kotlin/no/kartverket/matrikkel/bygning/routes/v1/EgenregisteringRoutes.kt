package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService

fun Route.egenregistreringRouting(egenregistreringsService: EgenregistreringsService) {
    route("/egenregistrering") {
        route("{bygningId}") {
            egenregistreringBygningIdDoc()
            get {
                val gyldigFra = call.request.queryParameters["gyldigFra"]?.toLong() ?: 0L
                val bygningId = call.parameters["bygningId"]

                if (bygningId == null) {
                    call.respondText("Du må sende med bygningId som parameter", status = HttpStatusCode.BadRequest)
                    return@get
                }

                val egenregistreringerForBygning =
                    egenregistreringsService.getEgenregistreringerForBygning(bygningId, gyldigFra)

                if (egenregistreringerForBygning != null) {
                    call.respond(egenregistreringerForBygning)
                } else {
                    call.respondText("Fant ingen bygninger med id $bygningId", status = HttpStatusCode.NotFound)
                }

            }
            post {
                val egenregistrering = call.receive<Egenregistrering>()

                val bygningId = call.parameters["bygningId"]

                if (bygningId == null) {
                    call.respondText("Du må sende med bygningId som parameter", status = HttpStatusCode.BadRequest)
                    return@post
                }

                val addedEgenregistrering = egenregistreringsService.addEgenregistreringToBygning(bygningId, egenregistrering)

                if (addedEgenregistrering) {
                    call.respondText(
                        "Egenregistrering registrert på bygning $bygningId", status = HttpStatusCode.OK
                    )
                } else {
                    call.respondText(
                        "Det ble forsøkt registrert egenregistreringer på bruksenheter som ikke har tilknytning til bygningen",
                        status = HttpStatusCode.BadRequest
                    )
                }
            }
        }
    }
}

private fun Route.egenregistreringBygningIdDoc() {
    install(NotarizedRoute()) {
        parameters = listOf(
            Parameter(
                name = "bygningId", `in` = Parameter.Location.path, schema = TypeDefinition.STRING
            )
        )
        post = PostInfo.builder {
            summary("Legg til en egenregistrering på en bygning")
            description("Legger til en egenregistrering på en bygning og tilhørende bruksenheter, hvis noen")
            request {
                requestType<Egenregistrering>()
                required(true)
                description("Egeneregistrert data")
            }
            response {
                responseCode(HttpStatusCode.OK)
                responseType<String>()
                description("Bygninger og eventuelle bruksenheter registrert")
            }

            canRespond {
                responseCode(HttpStatusCode.BadRequest)
                responseType<String>()
                description("Alle bruksenheter som kom i request tilhørte ikke bygningen")
            }
        }

        get = GetInfo.builder {
            summary("Hent egenregistreringer for en bygning")
            description("Henter egenregistreringer på en bygning gitt en gyldighetsdato. Henter ikke tilhørende bruksenheter. Uten gyldighetsdato henter den alle registreringer")

            parameters(
                Parameter(
                    name = "gyldigFra", `in` = Parameter.Location.query, schema = TypeDefinition.LONG, required = false
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