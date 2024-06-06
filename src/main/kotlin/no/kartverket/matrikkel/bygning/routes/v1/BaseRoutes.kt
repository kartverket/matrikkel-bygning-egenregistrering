package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.routes.swagger
import io.ktor.server.application.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService

fun Application.baseRoutesV1(
    egenregistreringsService: EgenregistreringsService
) {
    routing {
        swagger()

        route("/v1") {
            egenregistreringRouting(egenregistreringsService)
        }
    }
}