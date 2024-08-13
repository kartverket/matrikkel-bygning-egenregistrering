package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
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
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringValidationError
import no.kartverket.matrikkel.bygning.models.requests.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.models.requests.RegistreringMetadataRequest
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.models.responses.ErrorResponse
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

            val validationErrors = EgenregistreringsService.validateEgenregistreringRequest(egenregistrering).toMutableList()

            val bygningFromMatrikkel = bygningClient.getBygningById(bygningId.toLong())

            if (bygningFromMatrikkel == null) {
                validationErrors.add(
                    ErrorDetail(
                        detail = EgenregistreringValidationError.BygningDoesNotExist.errorMessage,
                    ),
                )
            }

            if (validationErrors.isNotEmpty()) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse.ValidationError(
                        correlationId = call.callId,
                        errorDetails = validationErrors,
                    ),
                )
                return@post
            }

            if (bygningFromMatrikkel != null) {
                val addedEgenregistrering = egenregistreringsService.addEgenregistreringToBygning(bygningFromMatrikkel, egenregistrering)

                if (addedEgenregistrering) {
                    call.respondText(
                        "Egenregistrering registrert på bygning $bygningId", status = HttpStatusCode.Created,
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        ErrorResponse.ValidationError(
                            correlationId = call.callId,
                            errorDetails = listOf(
                                ErrorDetail(
                                    detail = EgenregistreringValidationError.BruksenhetIsNotConnectedToBygning.errorMessage,
                                ),
                            ),
                        ),
                    )
                }
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

            // Kompendium støtter ikke sealed classes per nå på grunn av en bug som egentlig er løst, men han får ikke publisha til Maven
            // Se GitHub Issue for mer info: https://github.com/bkbnio/kompendium/issues/625
            // Eventuell workaround er å gjøre ErrorResponse til interface
            /*
            canRespond {
                responseCode(HttpStatusCode.BadRequest)
                responseType<ErrorResponse.ValidationError>()
                description("Validering av egenregistreringsdata har gått feil")
            }
            */
        }
    }
}
