package no.kartverket.matrikkel.bygning.routes.v1.egenregistrering

import com.github.michaelbull.result.fold
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.routes.common.ErrorResponse
import no.kartverket.matrikkel.bygning.routes.common.toErrorDetailResponse

fun Route.egenregistreringRouting(egenregistreringService: EgenregistreringService) {
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
