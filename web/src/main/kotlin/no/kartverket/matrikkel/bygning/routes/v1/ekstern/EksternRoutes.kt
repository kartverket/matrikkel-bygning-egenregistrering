package no.kartverket.matrikkel.bygning.routes.v1.ekstern

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.bygning.bygningEksternRouting

fun Route.eksternRouting(
    bygningService: BygningService,
) {
    authenticate("maskinporten") {
        route("/ekstern") {
            bygningEksternRouting(bygningService)
        }
    }
}
