package no.kartverket.matrikkel.bygning.routes.v1

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.routing.route

fun Application.baseRoutesV1() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        route("/v1") {
            bygningRouting()
        }
    }
}