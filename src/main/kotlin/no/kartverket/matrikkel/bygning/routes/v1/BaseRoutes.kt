package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.routes.swagger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.services.BygningService
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService
import org.koin.ktor.ext.inject

fun Application.installBaseRouting() {
    routing {
        swagger()

        // TODO Remove after checking connection between Egenreg and Bygning
        route("dummy") {
            get {
                call.respondText("Hei Egenregistrering!", status = HttpStatusCode.OK)
            }
        }

        route("v1") {
            val bygningService by inject<BygningService>()
            val egenregistreringsService by inject<EgenregistreringsService>()

            bygningRouting(bygningService, egenregistreringsService)
            kodelisteRouting()
        }

    }
}