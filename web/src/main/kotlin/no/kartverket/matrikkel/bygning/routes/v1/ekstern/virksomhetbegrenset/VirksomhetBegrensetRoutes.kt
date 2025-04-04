package no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetbegrenset

import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetbegrenset.bygning.bruksenhetVirksomhetBegrensetRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetbegrenset.bygning.bygningVirksomhetBegrensetRouting

fun Route.virksomhetBegrensetRouting(bygningService: BygningService) {
    route("bygninger") {
        bygningVirksomhetBegrensetRouting(bygningService)
    }
    route("bruksenheter") {
        bruksenhetVirksomhetBegrensetRouting(bygningService)
    }
}
