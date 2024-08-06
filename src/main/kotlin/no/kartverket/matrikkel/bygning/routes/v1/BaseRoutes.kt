package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.routes.swagger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService

fun Application.installBaseRouting(
    bygningClient: BygningClient,
    egenregistreringsService: EgenregistreringsService
) {
    routing {
        swagger()

        // TODO Remove after checking connection between Egenreg and Bygning
        route("dummy") {
            get {
                call.respondText("Hei Egenregistrering!", status = HttpStatusCode.OK)
            }
        }

        route("v1") {
            bygningRouting(bygningClient, egenregistreringsService)
            kodelisteRouting()
        }

    }
}
