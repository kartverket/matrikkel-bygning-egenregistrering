package no.kartverket.matrikkel.bygning.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.services.BygningService

fun Route.bygningRouting(bygningService: BygningService) {
    route("/bygning") {
        get {
            val bygninger = bygningService.getBygninger()

            call.respond(bygninger)
        }
        post {
            val bygning = call.receive<Bygning>()

            if (bygningService.addBygning(bygning)) {
                call.respond(HttpStatusCode.Created, bygning.id)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }

        }
    }
}