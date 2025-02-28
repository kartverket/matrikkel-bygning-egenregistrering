package no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning

import com.github.michaelbull.result.mapBoth
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.common.domainErrorToResponse

fun Route.bygningMedPersondataRouting(
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
                        body<BygningMedPersondataResponse> {
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

            val (status, body) = bygningService.getBygningByBubbleId(bygningId).mapBoth(
                success = { HttpStatusCode.OK to it.toBygningMedPersondataResponse() },
                failure = ::domainErrorToResponse,
            )

            call.respond(status, body)
        }
    }
}


fun Route.bruksenhetMedPersondataRouting(bygningService: BygningService) {
    route("{bruksenhetId}") {
        get(
            {
                summary = "Henter en bruksenhet"
                description = "Henter en bruksenhet"
                request {
                    pathParameter<String>("bruksenhetId") {
                        required = true
                    }
                }
                response {
                    code(HttpStatusCode.OK) {
                        body<BruksenhetMedPersondataResponse> {
                            description = "Bruksenheten"
                        }
                        description = "Bruksenheten finnes og ble hentet"
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Fant ikke bruksenhet med gitt bruksenhetId"
                    }
                }
            },
        ) {
            val bruksenhetId = call.parameters.getOrFail("bruksenhetId").toLong()

            val (status, body) = bygningService.getBruksenhetByBubbleId(bruksenhetId).mapBoth(
                success = { HttpStatusCode.OK to it.toBruksenhetMedPersondataResponse() },
                failure = ::domainErrorToResponse,
            )

            call.respond(status, body)
        }
    }
}
