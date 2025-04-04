package no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet

import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.bruksenhetVirksomhetUtvidetRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.bygningVirksomhetUtvidetRouting

fun Route.virksomhetUtvidetRouting(bygningService: BygningService) {
    route("bygninger") {
        bygningVirksomhetUtvidetRouting(bygningService)
    }
    route("bruksenheter") {
        bruksenhetVirksomhetUtvidetRouting(bygningService)
    }
}
