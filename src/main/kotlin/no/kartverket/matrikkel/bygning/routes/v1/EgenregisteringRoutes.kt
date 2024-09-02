package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.models.Result.ErrorResult
import no.kartverket.matrikkel.bygning.models.Result.Success
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.responses.ErrorResponse
import no.kartverket.matrikkel.bygning.routes.v1.dto.request.BruksenhetRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.dto.request.BygningRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.dto.request.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.services.EgenregistreringService

fun Route.egenregistreringRouting(egenregistreringService: EgenregistreringService) {
    egenregistreringDoc()

    post {
        val egenregistrering = call.receive<EgenregistreringRequest>()

        when (val result = egenregistreringService.addEgenregistreringToBygning(egenregistrering)) {
            is Success -> call.respond(HttpStatusCode.Created)
            is ErrorResult -> call.respond(
                status = HttpStatusCode.BadRequest,
                ErrorResponse.ValidationError(
                    details = result.errors,
                ),
            )
        }
    }
}

private fun Route.egenregistreringDoc() {
    install(NotarizedRoute()) {
        tags = setOf("Egenregistrering")
        post = PostInfo.builder {
            summary("Legg til en egenregistrering på en bygning")
            description("Legger til en egenregistrering på en bygning og tilhørende bruksenheter, hvis noen")
            request {
                requestType<EgenregistreringRequest>()
                required(true)
                description("Egenregistrert data")
                examples(
                    "Bygning Id 1" to EgenregistreringRequest(
                        bygningId = 1,
                        bygningRegistrering = BygningRegistreringRequest(
                            bruksarealRegistrering = BruksarealRegistrering(
                                bruksareal = 125.0,
                            ),
                            null,
                            null,
                            null,
                        ),
                        bruksenhetRegistreringer = listOf(
                            BruksenhetRegistreringRequest(
                                bruksenhetId = 1L,
                                null,
                                energikildeRegistrering = EnergikildeRegistrering(
                                    energikilder = listOf(EnergikildeKode.Elektrisitet, EnergikildeKode.Gass),
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
