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
import io.ktor.server.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import no.kartverket.matrikkel.bygning.models.Result.ErrorResult
import no.kartverket.matrikkel.bygning.models.Result.Success
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.requests.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.models.requests.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.models.requests.RegistreringMetadataRequest
import no.kartverket.matrikkel.bygning.models.responses.ErrorResponse
import no.kartverket.matrikkel.bygning.services.EgenregistreringService

fun Route.egenregistreringRouting(egenregistreringService: EgenregistreringService) {
    route("{bygningId}/egenregistreringer") {
        egenregistreringBygningIdDoc()

        post {
            val egenregistrering = call.receive<EgenregistreringRequest>()
            val bygningId = call.parameters.getOrFail("bygningId").toLong()

            when (val result = egenregistreringService.addEgenregistreringToBygning(bygningId, egenregistrering)) {
                is Success -> {
                    call.respond(
                        HttpStatusCode.Created,
                    )
                }

                is ErrorResult -> call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse.ValidationError(
                        details = result.errors,
                    ),
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
                        bygningRegistrering = BygningRegistrering(
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
                responseCode(HttpStatusCode.Created)
                responseType<Unit>()
                description("Egenregistrering ble registrert")
            }

            canRespond {
                responseCode(HttpStatusCode.BadRequest)
                responseType<ErrorResponse.ValidationError>()
                description("Validering av egenregistreringsdata har gått feil")
            }

            canRespond {
                responseCode(HttpStatusCode.InternalServerError)
                responseType<ErrorResponse.InternalServerError>()
                description("Noe gikk galt på server")
            }
        }
    }
}
