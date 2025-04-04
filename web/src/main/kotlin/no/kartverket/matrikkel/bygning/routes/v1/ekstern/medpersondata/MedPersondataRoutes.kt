package no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata

import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.bruksenhetMedPersondataRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.bygningMedPersondataRouting

fun Route.bygningMedPersondataRouting(bygningService: BygningService) {
    route("bygninger") {
        bygningMedPersondataRouting(bygningService)
    }
    route("bruksenheter") {
        bruksenhetMedPersondataRouting(bygningService)
    }
}
