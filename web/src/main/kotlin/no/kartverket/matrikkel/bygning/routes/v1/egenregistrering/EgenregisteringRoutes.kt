package no.kartverket.matrikkel.bygning.routes.v1.egenregistrering

import com.github.michaelbull.result.fold
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.routes.common.ErrorResponse
import no.kartverket.matrikkel.bygning.routes.common.toErrorDetailResponse

fun Route.egenregistreringRouting(egenregistreringService: EgenregistreringService) {
    post(
        {
            summary = "Legg til en egenregistrering på en bygning"
            description = "Legger til en egenregistrering på en bygning og tilhørende bruksenheter, hvis noen"
            request {
                body<EgenregistreringRequest> {
                    required = true
                    example("Bygning ID 1") {
                        description = "Bygning med ID 1"
                        value = EgenregistreringRequest(
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
                        )
                    }
                }
            }

            response {
                code(HttpStatusCode.Created) {
                    description = "Egenregistrering ble registrert"
                }
                code(HttpStatusCode.BadRequest) {
                    body<ErrorResponse.ValidationError>()
                    description = "Validering av egenregistreringsdata har gått feil"
                }
                code(HttpStatusCode.InternalServerError) {
                    body<ErrorResponse.InternalServerError>()
                    description = "Noe gikk galt på server"
                }
            }
        },
    ) {
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
