package no.kartverket.matrikkel.bygning.routes.v1.intern.bygning

import com.github.michaelbull.result.mapBoth
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.util.getOrFail
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.common.domainErrorToResponse

fun Route.bygningRouting(bygningService: BygningService) {
    route(
        "{bygningId}",
        {
            request {
                pathParameter<String>("bygningId") {
                    required = true
                }
            }
            response {
                code(HttpStatusCode.NotFound) {
                    description = "Fant ikke bygning med gitt bygningId"
                }
            }
        },
    ) {
        get(
            {
                summary = "Henter en bygning"
                description = "Henter en bygning med tilhørende bruksenheter"
                response {
                    code(HttpStatusCode.OK) {
                        body<BygningInternResponse> {
                            description = "Bygningen med tilhørende bruksenheter"
                        }
                        description = "Bygningen finnes og ble hentet"
                    }
                }
            },
        ) {
            val bygningId = call.parameters.getOrFail("bygningId").toLong()

            val (status, body) =
                bygningService
                    .getBygningByBubbleId(bygningBubbleId = bygningId)
                    .mapBoth(
                        success = { HttpStatusCode.OK to it.toBygningInternResponse() },
                        failure = ::domainErrorToResponse,
                    )

            call.respond(status, body)
        }

        route("egenregistrert") {
            get(
                {
                    summary = "Hent egenregistrert data for en bygning"
                    description = "Hent egenregistrert data for en bygning med tilhørende bruksenheter"
                    response {
                        code(HttpStatusCode.OK) {
                            body<BygningSimpleResponse> {
                                description = "Bygning med én datakilde - kun egenregistrerte data"
                            }
                            description = "Bygningen finnes og ble hentet"
                        }
                    }
                },
            ) {
                val bygningId = call.parameters.getOrFail("bygningId").toLong()

                val (status, body) =
                    bygningService.getBygningByBubbleId(bygningBubbleId = bygningId).mapBoth(
                        success = { HttpStatusCode.OK to it.toBygningSimpleResponseFromEgenregistrertData() },
                        failure = ::domainErrorToResponse,
                    )

                call.respond(status, body)
            }
        }
    }
}
