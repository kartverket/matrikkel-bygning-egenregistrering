package no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii

import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.bruksenhetVirksomhetUtvidetUtenPIIRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.bygningVirksomhetUtvidetUtenPIIRouting

fun Route.virksomhetUtvidetUtenPIIRouting(bygningService: BygningService) {
    route("bygninger") {
        bygningVirksomhetUtvidetUtenPIIRouting(bygningService)
    }
    route("bruksenheter") {
        bruksenhetVirksomhetUtvidetUtenPIIRouting(bygningService)
    }
}
