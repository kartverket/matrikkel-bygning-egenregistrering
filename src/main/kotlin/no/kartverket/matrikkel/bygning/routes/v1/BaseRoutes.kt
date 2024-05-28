package no.kartverket.matrikkel.bygning.routes.v1

import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.routing.route
import no.kartverket.matrikkel.bygning.services.BygningService

fun Application.baseRoutesV1(
    bygningService: BygningService
) {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")

        route("/v1") {
            bygningRouting(bygningService)
        }
    }
}