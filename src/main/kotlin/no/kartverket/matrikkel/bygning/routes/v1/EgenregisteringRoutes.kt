package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.requests.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BygningsRegistrering
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.models.requests.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.models.requests.RegistreringMetadataRequest
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService

fun Route.egenregistreringRouting(
    bygningClient: BygningClient, egenregistreringsService: EgenregistreringsService
) {

    route("{bygningId}/egenregistreringer") {
        egenregistreringBygningIdDoc()

        post {
            val egenregistrering = call.receive<EgenregistreringRequest>()

            val bygningId = call.parameters["bygningId"]

            if (bygningId == null) {
                call.respondText("Du må sende med bygningId som parameter", status = HttpStatusCode.BadRequest)
                return@post
            }

            val bygningFromMatrikkel = bygningClient.getBygningById(bygningId.toLong())

            if (bygningFromMatrikkel == null) {
                call.respondText("Bygningen finnes ikke i matrikkelen", status = HttpStatusCode.BadRequest)
                return@post
            }

            val addedEgenregistrering = egenregistreringsService.addEgenregistreringToBygning(bygningFromMatrikkel, egenregistrering)

            if (addedEgenregistrering) {
                call.respondText(
                    "Egenregistrering registrert på bygning $bygningId", status = HttpStatusCode.OK,
                )
            } else {
                call.respondText(
                    "Det ble forsøkt registrert egenregistreringer på bruksenheter som ikke har tilknytning til bygningen",
                    status = HttpStatusCode.BadRequest,
                )
            }
        }
    }
}

private fun Route.egenregistreringBygningIdDoc() {
    install(NotarizedRoute()) {
        tags = setOf("Egenregistrering")
        parameters = listOf(
            Parameter(
                name = "bygningId", `in` = Parameter.Location.path, schema = TypeDefinition.STRING,
            ),
        )
        post = PostInfo.builder {
            summary("Legg til en egenregistrering på en bygning")
            description("Legger til en egenregistrering på en bygning og tilhørende bruksenheter, hvis noen")
            request {
                requestType<EgenregistreringRequest>()
                required(true)
                description("Egenregistrert data")
                examples(
                    "Bygning Id 1" to EgenregistreringRequest(
                        bygningsRegistrering = BygningsRegistrering(
                            bruksareal = BruksarealRegistrering(
                                bruksareal = 125.0,
                                metadata = RegistreringMetadataRequest(
                                    registreringstidspunkt = Clock.System.now(),
                                    gyldigFra = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                                    gyldigTil = null,
                                ),
                            ),
                            null,
                            null,
                            null,
                        ),
                        bruksenhetRegistreringer = listOf(
                            BruksenhetRegistrering(
                                bruksenhetId = 1L,
                                null,
                                energikilde = EnergikildeRegistrering(
                                    energikilder = listOf(EnergikildeKode.Elektrisitet, EnergikildeKode.Gass),
                                    metadata = RegistreringMetadataRequest(
                                        registreringstidspunkt = Clock.System.now(),
                                        gyldigFra = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                                        gyldigTil = null,
                                    ),
                                ),
                                null,
                            ),
                        ),
                    ),
                )

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
    }
}
