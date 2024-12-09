package no.kartverket.matrikkel.bygning.routes.v1.ekstern.bygning

import com.github.michaelbull.result.mapBoth
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.common.domainErrorToResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BygningResponse

fun Route.bygningEksternRouting(
    bygningService: BygningService
) {
    route("{bygningId}") {
        get(
            {
                summary = "Henter en bygning"
                description = "Henter en bygning med tilhørende bruksenheter"
                request {
                    pathParameter<String>("bygningId") {
                        required = true
                    }
                }
                response {
                    code(HttpStatusCode.OK) {
                        body<BygningResponse> {
                            description = "Bygningen med tilhørende bruksenheter"
                        }
                        description = "Bygningen finnes og ble hentet"
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Fant ikke bygning med gitt bygningId"
                    }
                }
            },
        ) {
            val bygningId = call.parameters.getOrFail("bygningId").toLong()

            val (status, body) = bygningService.getBygningWithEgenregistrertData(bygningId).mapBoth(
                success = { HttpStatusCode.OK to it.toBygningEksternResponse() },
                failure = ::domainErrorToResponse,
            )

            call.respond(status, body)
        }
    }
}
