package no.kartverket.matrikkel.bygning.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.kartverket.matrikkel.bygning.application.health.HealthService

fun Application.internalRouting(
    meterRegistry: PrometheusMeterRegistry,
    healthService: HealthService,
) {
    routing {
        route("/metrics") {
            get {
                call.respondText(meterRegistry.scrape())
            }
        }

        route("/healthz") {
            get {
                val isConnectionToDBHealthy = healthService.isHealthy()

                if (isConnectionToDBHealthy) {
                    call.respondText("Healthy", status = HttpStatusCode.OK)
                } else {
                    call.respondText("Unhealthy", status = HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}
