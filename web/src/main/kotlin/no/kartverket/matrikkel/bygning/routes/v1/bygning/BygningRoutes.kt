package no.kartverket.matrikkel.bygning.routes.v1.bygning

import com.github.michaelbull.result.fold
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService

fun Route.bygningRouting(
    bygningService: BygningService
) {
    route("{bygningId}") {
        get(
            {
                summary = "Henter en bygning"
                description =
                    "Henter en bygning med en gitt bygningId"
                request {
                    queryParameter<String>("bygningId") {
                        required = true
                    }
                }
                response {
                    code(HttpStatusCode.OK) {
                        body<BygningResponse> {
                            description = "Bygningen"
                        }
                        description = "Bygningen finnes og ble hentet"
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Bygningen finnes ikke"
                    }
                }
            },
        ) {
            val bygningId = call.parameters.getOrFail("bygningId").toLong()

            bygningService.getBygningWithEgenregistrertData(bygningId).fold(
                success = { call.respond(HttpStatusCode.OK, it.toBygningResponse()) },
                failure = {
                    call.respond(
                        HttpStatusCode.NotFound,
                        it.detail,
                    )
                },
            )
        }

        route("bruksenheter") {
            route("{bruksenhetId}") {
                get(
                    {
                        summary = "Henter en bruksenhet"
                        description =
                            "Henter en bruksenhet med en gitt bruksenhetId for en gitt bygning. Dersom bruksenheten ikke finnes på bygningen vil den ikke hentes"
                        request {
                            queryParameter<String>("bygningId") {
                                required = true
                            }
                            queryParameter<String>("bruksenhetId") {
                                required = true
                            }
                        }
                        response {
                            code(HttpStatusCode.OK) {
                                body<BruksenhetResponse> {
                                    description = "Bruksenheten"
                                }
                                description = "Bruksenheten finnes og ble hentet"
                            }
                            code(HttpStatusCode.NotFound) {
                                description = "Bruksenheten finnes ikke"
                            }
                        }
                    },
                ) {
                    val bygningId = call.parameters.getOrFail("bygningId").toLong()
                    val bruksenhetId = call.parameters.getOrFail("bruksenhetId").toLong()

                    bygningService.getBruksenhetWithEgenregistrertData(bygningId, bruksenhetId).fold(
                        success = { call.respond(HttpStatusCode.OK, it.toBruksenhetResponse()) },
                        failure = {
                            call.respond(
                                HttpStatusCode.NotFound,
                                it.detail,
                            )
                        },
                    )
                }
            }
        }
    }
}
