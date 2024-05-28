package no.kartverket.matrikkel.bygning.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bygningRouting() {
    route("/bygning") {
        get {
            call.respond(HttpStatusCode.NotFound, "Nothing")
        }
        post {
            call.respond(HttpStatusCode.OK, "Bueno")
        }
    }
}