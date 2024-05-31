package no.kartverket.matrikkel.bygning.routes.v1

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.services.BygningService
import no.kartverket.matrikkel.bygning.services.HealthService

fun Application.baseRoutesV1(
    bygningService: BygningService, healthService: HealthService
) {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        probeRouting(healthService)

        route("/v1") {
            bygningRouting(bygningService)
        }
    }
}