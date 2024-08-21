package no.kartverket.matrikkel.bygning

import io.bkbn.kompendium.core.routes.swagger
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.kartverket.matrikkel.bygning.config.loadConfiguration
import no.kartverket.matrikkel.bygning.db.createDataSource
import no.kartverket.matrikkel.bygning.db.runFlywayMigrations
import no.kartverket.matrikkel.bygning.matrikkel.createBygningClient
import no.kartverket.matrikkel.bygning.plugins.configureHTTP
import no.kartverket.matrikkel.bygning.plugins.configureMonitoring
import no.kartverket.matrikkel.bygning.plugins.configureOpenAPI
import no.kartverket.matrikkel.bygning.plugins.configureStatusPages
import no.kartverket.matrikkel.bygning.repositories.HealthRepository
import no.kartverket.matrikkel.bygning.routes.internalRouting
import no.kartverket.matrikkel.bygning.routes.v1.bygningRouting
import no.kartverket.matrikkel.bygning.routes.v1.egenregistreringRouting
import no.kartverket.matrikkel.bygning.routes.v1.kodelisteRouting
import no.kartverket.matrikkel.bygning.services.EgenregistreringService
import no.kartverket.matrikkel.bygning.services.HealthService

fun main() {
    val internalPort = System.getenv("INTERNAL_PORT")?.toIntOrNull() ?: 8081
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(
        factory = Netty,
        port = internalPort,
        module = Application::internalModule,
    ).start(wait = false)

    embeddedServer(
        factory = Netty,
        port = port,
        module = Application::mainModule,
        watchPaths = listOf("classes"),
    ).start(wait = true)
}

val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

fun Application.mainModule() {
    val config = loadConfiguration(environment)

    configureHTTP()
    configureMonitoring()
    configureOpenAPI()
    configureStatusPages()

    val dataSource = createDataSource(config)

    val bygningClient = createBygningClient(config)

    val egenregistreringService = EgenregistreringService(bygningClient)

    routing {
        swagger()

        route("v1") {
            kodelisteRouting()
            route("bygninger") {
                bygningRouting(bygningClient)
                egenregistreringRouting(egenregistreringService)
            }
        }
    }

    runFlywayMigrations(dataSource)
}

fun Application.internalModule() {
    val config = loadConfiguration(environment)

    configureMonitoring()

    val dataSource = createDataSource(config)
    val healthRepository = HealthRepository(dataSource)
    val healthService = HealthService(healthRepository)

    internalRouting(meterRegistry, healthService)
}
