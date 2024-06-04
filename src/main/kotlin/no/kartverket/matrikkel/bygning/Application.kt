package no.kartverket.matrikkel.bygning

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
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
import no.kartverket.matrikkel.bygning.routes.v1.probeRouting
import no.kartverket.matrikkel.bygning.services.BygningService
import no.kartverket.matrikkel.bygning.services.HealthService

val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::internalModule).start(wait = false)

    EngineMain.main(args)
}


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

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }

    DatabaseSingleton.init()
    val dbConnection = DatabaseSingleton.getConnection()
    val bygningRepository = BygningRepository(dbConnection)
    val bygningService = BygningService(bygningRepository)

    baseRoutesV1(bygningService)
}

fun Application.internalModule() {
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }

    DatabaseSingleton.init()
    val dbConnection = DatabaseSingleton.getConnection()
    val healthRepository = HealthRepository(dbConnection)
    val healthService = HealthService(healthRepository)

    routing {
        get("/metrics") {
            call.respondText(appMicrometerRegistry.scrape())
        }

        probeRouting(healthService)
    }

}
