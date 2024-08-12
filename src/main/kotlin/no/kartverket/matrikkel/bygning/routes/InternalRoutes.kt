package no.kartverket.matrikkel.bygning.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.kartverket.matrikkel.bygning.services.HealthService

fun Application.internalRouting(
    meterRegistry: PrometheusMeterRegistry,
    healthService: HealthService
) {
    routing {
        route("/metrics") {
            get {
                call.respondText(meterRegistry.scrape())
            }
        }

        route("/healthz") {
            get {
                val isConnectionToDBHealthy = healthService.getHealth()

                if (isConnectionToDBHealthy) {
                    call.respondText("Healthy", status = HttpStatusCode.OK)
                } else {
                    call.respondText("Unhealthy", status = HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}
