package no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata

import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.bruksenhetUtenPersondataRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.bygningUtenPersondataRouting

fun Route.utenPersondataRouting(bygningService: BygningService) {
    route("bygninger") {
        bygningUtenPersondataRouting(bygningService)
    }
    route("bruksenheter") {
        bruksenhetUtenPersondataRouting(bygningService)
    }
}
