package no.kartverket.matrikkel.bygning.routes.v1.ekstern.berettigetinteresse.bygning

import com.github.michaelbull.result.mapBoth
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.util.getOrFail
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.common.domainErrorToResponse

fun Route.bygningBerettigetInteresseRouting(bygningService: BygningService) {
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
                        body<BygningBerettigetInteresseResponse> {
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

            val (status, body) =
                bygningService.getBygningByBubbleId(bygningId).mapBoth(
                    success = { HttpStatusCode.OK to it.toBygningBerettigetInteresseResponse() },
                    failure = ::domainErrorToResponse,
                )

            call.respond(status, body)
        }
    }
}

fun Route.bruksenhetBerettigetInteresseRouting(bygningService: BygningService) {
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
                        body<BruksenhetBerettigetInteresseResponse> {
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

            val (status, body) =
                bygningService.getBruksenhetByBubbleId(bruksenhetId).mapBoth(
                    success = { HttpStatusCode.OK to it.toBruksenhetBerettigetInteresseResponse() },
                    failure = ::domainErrorToResponse,
                )

            call.respond(status, body)
        }
    }
}
