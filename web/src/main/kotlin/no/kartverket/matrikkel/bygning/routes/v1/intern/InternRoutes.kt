package no.kartverket.matrikkel.bygning.routes.v1.intern

import io.ktor.server.routing.Route
import io.github.smiley4.ktorswaggerui.dsl.routing.route
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.bygningRouting
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.egenregistreringRouting
import no.kartverket.matrikkel.bygning.routes.v1.kodeliste.kodelisteRouting

fun Route.internRouting(
    egenregistreringService: EgenregistreringService,
    bygningService: BygningService
) {
    route(
        "/",
        {
            specId = "intern"
        },
    ) {
        route("kodelister") {
            kodelisteRouting()
        }
        route("egenregistreringer") {
            egenregistreringRouting(egenregistreringService)
        }
        route("bygninger") {
            bygningRouting(bygningService)
        }
    }
}
