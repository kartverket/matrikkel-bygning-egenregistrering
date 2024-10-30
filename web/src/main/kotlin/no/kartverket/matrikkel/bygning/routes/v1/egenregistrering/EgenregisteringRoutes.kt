package no.kartverket.matrikkel.bygning.routes.v1.egenregistrering

import com.github.michaelbull.result.fold
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.core.plugin.NotarizedRoute.invoke
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.routes.common.ErrorResponse
import no.kartverket.matrikkel.bygning.routes.common.toErrorDetailResponse

fun Route.egenregistreringRouting(egenregistreringService: EgenregistreringService) {
    egenregistreringDoc()

    post {
        val egenregistreringRequest = call.receive<EgenregistreringRequest>()

        val egenregistrering = egenregistreringRequest.toEgenregistrering()

        egenregistreringService.addEgenregistrering(egenregistrering).fold(
            success = { call.respond(HttpStatusCode.Created) },
            failure = {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    ErrorResponse.ValidationError(
                        details = listOf(it.toErrorDetailResponse()),
                    ),
                )
            },
        )
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
                        eier = "31129956715",
                        bruksenhetRegistreringer = listOf(
                            BruksenhetRegistreringRequest(
                                bruksenhetId = 1L,
                                bruksarealRegistrering = null,
                                byggeaarRegistrering = null,
                                energikildeRegistrering = EnergikildeRegistreringRequest(
                                    energikilder = listOf(EnergikildeKode.Elektrisitet, EnergikildeKode.Gass),
                                ),
                                oppvarmingRegistrering = null,
                                vannforsyningRegistrering = null,
                                avlopRegistrering = null,
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
