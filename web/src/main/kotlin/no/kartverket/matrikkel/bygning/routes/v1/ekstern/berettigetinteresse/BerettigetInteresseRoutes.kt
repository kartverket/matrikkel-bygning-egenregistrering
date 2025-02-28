package no.kartverket.matrikkel.bygning.routes.v1.ekstern.berettigetinteresse

import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.berettigetinteresse.bygning.bruksenhetBerettigetInteresseRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.berettigetinteresse.bygning.bygningBerettigetInteresseRouting

fun Route.berettigetInteresseRouting(
    bygningService: BygningService
) {
    route("bygninger") {
        bygningBerettigetInteresseRouting(bygningService)
    }
    route("bruksenheter") {
        bruksenhetBerettigetInteresseRouting(bygningService)
    }
}
