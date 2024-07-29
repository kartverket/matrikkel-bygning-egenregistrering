package no.kartverket.matrikkel.bygning.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.appMicrometerRegistry
import no.kartverket.matrikkel.bygning.services.HealthService
import org.koin.ktor.ext.inject

fun Application.installInternalRouting() {
    routing {
        route("/metrics") {
            get {
                call.respondText(appMicrometerRegistry.scrape())
            }
        }


        route("/healthz") {
            val healthService: HealthService by inject()

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