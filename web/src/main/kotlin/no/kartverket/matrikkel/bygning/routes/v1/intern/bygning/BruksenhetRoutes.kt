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

fun Route.bruksenhetRouting(bygningService: BygningService) {
    route(
        "{bruksenhetId}",
        {
            request {
                pathParameter<String>("bruksenhetId") {
                    required = true
                }
            }
            response {
                code(HttpStatusCode.NotFound) {
                    description = "Fant ikke bruksenhet med gitt bruksenhetId"
                }
            }
        },
    ) {
        get(
            {
                summary = "Hent en bruksenhet"
                description = "Hent en bruksenhet"
                response {
                    code(HttpStatusCode.OK) {
                        body<BruksenhetInternResponse> {
                            description = "Bruksenhet"
                        }
                        description = "Bruksenheten finnes og ble hentet"
                    }
                }
            },
        ) {
            val bruksenhetId = call.parameters.getOrFail("bruksenhetId").toLong()

            val (status, body) =
                bygningService
                    .getBruksenhetByBubbleId(
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
                    response {
                        code(HttpStatusCode.OK) {
                            body<BruksenhetSimpleResponse> {
                                description = "Bruksenhet med én datakilde - kun egenregistrerte data"
                            }
                            description = "Bruksenheten finnes og ble hentet"
                        }
                    }
                },
            ) {
                val bruksenhetId = call.parameters.getOrFail("bruksenhetId").toLong()

                val (status, body) =
                    bygningService
                        .getBruksenhetByBubbleId(
                            bruksenhetBubbleId = bruksenhetId,
                        ).mapBoth(
                            success = { HttpStatusCode.OK to it.toBruksenhetSimpleResponseFromEgenregistrertData() },
                            failure = ::domainErrorToResponse,
                        )

                call.respond(status, body)
            }
        }
    }
}
