package no.kartverket.matrikkel.bygning

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.kartverket.matrikkel.bygning.db.DatabaseSingleton
import no.kartverket.matrikkel.bygning.repositories.BygningRepository
import no.kartverket.matrikkel.bygning.repositories.HealthRepository
import no.kartverket.matrikkel.bygning.routes.v1.baseRoutesV1
import no.kartverket.matrikkel.bygning.services.BygningService
import no.kartverket.matrikkel.bygning.services.HealthService

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    install(ContentNegotiation) {
        json()
        removeIgnoredType<String>()
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }

    install(CallLogging) {
        filter { call ->
            call.request.path().startsWith("/v1")
        }
    }

    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }

    routing {
        port(8081) {
            get("/metrics") {
                call.respondText(appMicrometerRegistry.scrape())
            }
        }
    }

    DatabaseSingleton.init()

    val dbConnection = DatabaseSingleton.getConnection() ?: throw RuntimeException("Kunne ikke koble til database")

    val bygningRepository = BygningRepository(dbConnection)
    val healthRepository = HealthRepository(dbConnection)

    val bygningService = BygningService(bygningRepository)
    val healthService = HealthService(healthRepository)

    baseRoutesV1(bygningService, healthService)
}
