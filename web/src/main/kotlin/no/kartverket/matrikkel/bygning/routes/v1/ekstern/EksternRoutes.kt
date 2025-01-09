package no.kartverket.matrikkel.bygning.routes.v1.ekstern

import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.bygning.bygningEksternRouting

fun Route.eksternRouting(
    bygningService: BygningService,
) {
    authenticate("maskinporten") {
        route(
            "/ekstern",
            {
                securitySchemeNames = listOf("maskinporten")
                response {
                    code(HttpStatusCode.Unauthorized) {
                        description = "Manglende eller ugyldig token"
                    }
                }
            },
        ) {
            route("bygninger") {
                bygningEksternRouting(bygningService)
            }
        }
    }
}
