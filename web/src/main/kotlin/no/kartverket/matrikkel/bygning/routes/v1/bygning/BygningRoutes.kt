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
                    "Henter en bygning med tilhørende bruksenheter"
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

        route("egenregistrert") {
            get(
                {
                    summary = "Hent egenregistrert data for en bygning"
                    description =
                        "Hent egenregistrert data for en bygning med tilhørende bruksenheter"
                    request {
                        pathParameter<String>("bygningId") {
                            required = true
                        }
                    }
                    response {
                        code(HttpStatusCode.OK) {
                            body<BygningSimpleResponse> {
                                description = "Bygning med én datakilde - kun egenregistrerte data"
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

                bygningService.getBygningWithEgenregistrertData(bygningId).fold(
                    success = { call.respond(HttpStatusCode.OK, it.toBygningSimpleResponseFromEgenregistrertData()) },
                    failure = {
                        call.respond(
                            HttpStatusCode.NotFound,
                            it.detail,
                        )
                    },
                )
            }
        }

        route("bruksenheter") {
            route("{bruksenhetId}") {
                get(
                    {
                        summary = "Hent en bruksenhet"
                        description = "Hent en bruksenhet"
                        request {
                            pathParameter<String>("bygningId") {
                                required = true
                            }
                            pathParameter<String>("bruksenhetId") {
                                required = true
                            }
                        }
                        response {
                            code(HttpStatusCode.OK) {
                                body<BruksenhetResponse> {
                                    description = "Bruksenhet"
                                }
                                description = "Bruksenheten finnes og ble hentet"
                            }
                            code(HttpStatusCode.NotFound) {
                                description = "Fant ikke bruksenhet med gitt bruksenhetId for gitt bygningId"
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

                route("egenregistrert") {
                    get(
                        {
                            summary = "Hent egenregistrert data for en bruksenhet"
                            description = "Hent egenregistrert data en bruksenhet"
                            request {
                                pathParameter<String>("bygningId") {
                                    required = true
                                }
                                pathParameter<String>("bruksenhetId") {
                                    required = true
                                }
                            }
                            response {
                                code(HttpStatusCode.OK) {
                                    body<BruksenhetSimpleResponse> {
                                        description = "Bruksenhet med én datakilde - kun egenregistrerte data"
                                    }
                                    description = "Bruksenheten finnes og ble hentet"
                                }
                                code(HttpStatusCode.NotFound) {
                                    description = "Fant ikke bruksenhet med gitt bruksenhetId for gitt bygningId"
                                }
                            }
                        },
                    ) {
                        val bygningId = call.parameters.getOrFail("bygningId").toLong()
                        val bruksenhetId = call.parameters.getOrFail("bruksenhetId").toLong()

                        bygningService.getBruksenhetWithEgenregistrertData(bygningId, bruksenhetId).fold(
                            success = { call.respond(HttpStatusCode.OK, it.toBruksenhetSimpleResponseFromEgenregistrertData()) },
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
}
