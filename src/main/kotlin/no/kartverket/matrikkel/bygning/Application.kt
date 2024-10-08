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
import no.kartverket.matrikkel.bygning.repositories.EgenregistreringRepository
import no.kartverket.matrikkel.bygning.repositories.HealthRepository
import no.kartverket.matrikkel.bygning.routes.internalRouting
import no.kartverket.matrikkel.bygning.routes.v1.bygning.bygningRouting
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.egenregistreringRouting
import no.kartverket.matrikkel.bygning.routes.v1.kodeliste.kodelisteRouting
import no.kartverket.matrikkel.bygning.services.BygningService
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

    val egenregistreringRepository = EgenregistreringRepository(dataSource)

    val bygningClient = createBygningClient(config)


    val egenregistreringService = EgenregistreringService(bygningClient, egenregistreringRepository)
    val bygningService = BygningService(bygningClient, egenregistreringService)

    routing {
        swagger()

        route("v1") {
            route("kodelister") {
                kodelisteRouting()
            }
            route("egenregistreringer") {
                egenregistreringRouting(egenregistreringService)
            }
            route("bygninger") {
                bygningRouting(bygningService)
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
