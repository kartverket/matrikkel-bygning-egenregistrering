package no.kartverket.matrikkel.bygning.routes.v1.ekstern

import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.bygning.bygningEksternRouting

fun Route.eksternRouting(
    bygningService: BygningService,
) {
    route("/ekstern") {
        route("bygninger") {
            bygningEksternRouting(bygningService)
        }
    }
}
