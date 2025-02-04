package no.kartverket.matrikkel.bygning.routes.v1.intern.bygning

import com.github.michaelbull.result.mapBoth
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.common.domainErrorToResponse
import no.kartverket.matrikkel.bygning.routes.v1.common.toInstant
import java.time.Instant

fun Route.bygningRouting(
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

            val (status, body) = bygningService.getBygningByBubbleId(bygningBubbleId = bygningId)
                .mapBoth(
                    success = { HttpStatusCode.OK to it.toBygningResponse() },
                    failure = ::domainErrorToResponse,
                )

            call.respond(status, body)
        }

        route("egenregistrert") {
            get(
                {
                    summary = "Hent egenregistrert data for en bygning"
                    description = "Hent egenregistrert data for en bygning med tilhørende bruksenheter"
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

                val (status, body) = bygningService.getBygningByBubbleId(bygningBubbleId = bygningId)
                    .mapBoth(
                        success = { HttpStatusCode.OK to it.toBygningSimpleResponseFromEgenregistrertData() },
                        failure = ::domainErrorToResponse,
                    )

                call.respond(status, body)
            }

            get(
                "arkiv",
                {
                    summary = "Hent egenregistrert data for en bygning for et gitt registreringstidspunkt"
                    description =
                        "Henter tidligere versjon av egenregistrert data for en bygning basert på informasjonsgrunnlaget ved gitt registreringstidspunkt"
                    request {
                        pathParameter<String>("bygningId") {
                            required = true
                        }
                        queryParameter<Instant>("registreringstidspunkt") {
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
                        code(HttpStatusCode.BadRequest) {
                            description = "Forespørselen inneholder ugyldige parametere. Kontroller at alle felt er korrekt formatert."
                        }
                    }
                },
            ) {
                val bygningId = call.parameters.getOrFail("bygningId").toLong()
                val registreringstidspunktQuery = call.request.queryParameters["registreringstidspunkt"]

                val registreringstidspunkt = registreringstidspunktQuery?.toInstant() ?: Instant.now()

                val (status, body) = bygningService.getBygningByBubbleId(
                    bygningBubbleId = bygningId,
                    registreringstidspunkt = registreringstidspunkt,
                ).mapBoth(
                    success = { HttpStatusCode.OK to it.toBygningSimpleResponseFromEgenregistrertData() },
                    failure = ::domainErrorToResponse,
                )

                call.respond(status, body)
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

                    val (status, body) = bygningService.getBruksenhetByBubbleId(
                        bygningBubbleId = bygningId,
                        bruksenhetBubbleId = bruksenhetId,
                    ).mapBoth(
                        success = { HttpStatusCode.OK to it.toBruksenhetResponse() },
                        failure = ::domainErrorToResponse,
                    )

                    call.respond(status, body)
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

                        val (status, body) = bygningService.getBruksenhetByBubbleId(
                            bygningBubbleId = bygningId,
                            bruksenhetBubbleId = bruksenhetId,
                        ).mapBoth(
                            success = { HttpStatusCode.OK to it.toBruksenhetSimpleResponseFromEgenregistrertData() },
                            failure = ::domainErrorToResponse,
                        )

                        call.respond(status, body)
                    }

                    get(
                        "arkiv",
                        {
                            summary = "Hent egenregistrert data for en bruksenhet for et gitt registreringstidspunkt"
                            description =
                                "Henter tidligere versjon av egenregistrert data for en bruksenhet basert på informasjonsgrunnlaget ved gitt registreringstidspunkt"
                            request {
                                pathParameter<String>("bygningId") {
                                    required = true
                                }
                                pathParameter<String>("bruksenhetId") {
                                    required = true
                                }
                                queryParameter<Instant>("date") {
                                    required = true
                                    description = "Default verdi: ${Instant.now()}"
                                }
                            }
                            response {
                                code(HttpStatusCode.OK) {
                                    body<BygningSimpleResponse> {
                                        description = "Bruksenhet med én datakilde - kun egenregistrerte data"
                                    }
                                    description = "Bruksenhet finnes og ble hentet"
                                }
                                code(HttpStatusCode.NotFound) {
                                    description = "Fant ikke bruksenhet med gitt bygningId"
                                }
                                code(HttpStatusCode.BadRequest) {
                                    description =
                                        "Forespørselen inneholder ugyldige parametere. Kontroller at alle felt er korrekt formatert."
                                }
                            }
                        },
                    ) {
                        val bygningId = call.parameters.getOrFail("bygningId").toLong()
                        val bruksenhetId = call.parameters.getOrFail("bruksenhetId").toLong()
                        val registreringstidspunktQuery = call.request.queryParameters["registreringstidspunkt"]

                        val registreringstidspunkt = registreringstidspunktQuery?.toInstant() ?: Instant.now()

                        val (status, body) = bygningService.getBruksenhetByBubbleId(
                            bygningBubbleId = bygningId,
                            bruksenhetBubbleId = bruksenhetId,
                            registreringstidspunkt = registreringstidspunkt,
                        ).mapBoth(
                            success = { HttpStatusCode.OK to it.toBruksenhetSimpleResponseFromEgenregistrertData() },
                            failure = ::domainErrorToResponse,
                        )

                        call.respond(status, body)
                    }
                }
            }
        }
    }
}
