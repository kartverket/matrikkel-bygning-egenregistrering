package no.kartverket.matrikkel.bygning.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.dummyRouting() {
    route("dummy") {
        get {
            call.respondText("Hei Egenregistrering!", status = HttpStatusCode.OK)
        }
    }
}
