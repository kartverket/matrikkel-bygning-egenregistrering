package no.kartverket.matrikkel.bygning.routes.v1.ekstern.bygning

import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService

fun Route.bygningEksternRouting(
    bygningService: BygningService
) {
    route("bygning") {

    }
//    route("{bygningId}") {
//        get(
//            {
//                summary = "Henter en bygning"
//                description = "Henter en bygning med tilhørende bruksenheter"
//                request {
//                    pathParameter<String>("bygningId") {
//                        required = true
//                    }
//                }
//                response {
//                    code(HttpStatusCode.OK) {
//                        body<BygningEksternResponse> {
//                            description = "Bygningen med tilhørende bruksenheter"
//                        }
//                        description = "Bygningen finnes og ble hentet"
//                    }
//                    code(HttpStatusCode.NotFound) {
//                        description = "Fant ikke bygning med gitt bygningId"
//                    }
//                }
//            },
//        ) {
//            val bygningId = call.parameters.getOrFail("bygningId").toLong()
//
//            val (status, body) = bygningService.getBygningWithEgenregistrertData(bygningId).mapBoth(
//                success = { HttpStatusCode.OK to it.toBygningEksternResponse() },
//                failure = ::domainErrorToResponse,
//            )
//
//            call.respond(status, body)
//        }
//    }
}

