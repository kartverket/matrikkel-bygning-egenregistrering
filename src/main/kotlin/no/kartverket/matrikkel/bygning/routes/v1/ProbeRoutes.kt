package no.kartverket.matrikkel.bygning.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.services.HealthService

fun Route.probeRouting(healthService: HealthService) {
    route("/healthz") {
        get {
            val isConnectionToDBHealthy = healthService.getHealth();

            if (isConnectionToDBHealthy) {
                call.respondText("Healthy", status = HttpStatusCode.OK)
            } else {
                call.respondText("Unhealthy", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}