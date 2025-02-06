package no.kartverket.matrikkel.bygning

import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.application.health.HealthService
import no.kartverket.matrikkel.bygning.config.Env
import no.kartverket.matrikkel.bygning.config.loadConfiguration
import no.kartverket.matrikkel.bygning.infrastructure.database.DatabaseConfig
import no.kartverket.matrikkel.bygning.infrastructure.database.createDataSource
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.EgenregistreringRepositoryImpl
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.HealthRepositoryImpl
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.bygning.BygningRepositoryImpl
import no.kartverket.matrikkel.bygning.infrastructure.database.runFlywayMigrations
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.MatrikkelApi
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client.LocalBygningClient
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client.MatrikkelBygningClient
import no.kartverket.matrikkel.bygning.plugins.OpenApiSpecIds
import no.kartverket.matrikkel.bygning.plugins.authentication.configureAuthentication
import no.kartverket.matrikkel.bygning.plugins.configureHTTP
import no.kartverket.matrikkel.bygning.plugins.configureMonitoring
import no.kartverket.matrikkel.bygning.plugins.configureOpenAPI
import no.kartverket.matrikkel.bygning.plugins.configureStatusPages
import no.kartverket.matrikkel.bygning.routes.internalRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.eksternRouting
import no.kartverket.matrikkel.bygning.routes.v1.intern.internRouting
import java.net.URI

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
    configureAuthentication(config)

    val dataSource = createDataSource(
        DatabaseConfig(
            databaseUrl = config.property("storage.jdbcURL").getString(),
            username = config.property("storage.username").getString(),
            password = config.property("storage.password").getString(),
        ),
    )

    val bygningClient =
        if (Env.isLocal() && config.property("matrikkel.useStub").getString().toBoolean()) {
            log.warn("Bruker stub for matrikkel APIet. Skal kun brukes lokalt!")
            LocalBygningClient()
        } else {
            MatrikkelBygningClient(
                MatrikkelApi(
                    URI(config.property("matrikkel.baseUrl").getString()),
                ).withAuth(
                    config.property("matrikkel.username").getString(),
                    config.property("matrikkel.password").getString(),
                ),
            )
        }

    val egenregistreringRepository = EgenregistreringRepositoryImpl(dataSource)
    val bygningRepository = BygningRepositoryImpl(dataSource)

    val bygningService = BygningService(
        bygningClient = bygningClient,
        bygningRepository = bygningRepository,
    )

    val egenregistreringService = EgenregistreringService(
        bygningService = bygningService,
        egenregistreringRepository = egenregistreringRepository,
    )

    routing {
        // Routes for interne endepunkter.
        route("api.json") {
            openApiSpec(OpenApiSpecIds.INTERN)
        }
        route("swagger-ui") {
            swaggerUI("/api.json")
        }
        route("v1") {
            internRouting(egenregistreringService, bygningService)
        }

        // Routes for eksterne endepunkter.
        route("ekstern") {
            route("api.json") {
                openApiSpec(OpenApiSpecIds.EKSTERN)
            }
            route("swagger-ui") {
                swaggerUI("/ekstern/api.json")
            }
        }
        route("v1") {
            eksternRouting(bygningService)

        }
    }
    runFlywayMigrations(dataSource)
}

fun Application.internalModule() {
    val config = loadConfiguration(environment)

    configureMonitoring()

    val dataSource = createDataSource(
        DatabaseConfig(
            databaseUrl = config.property("storage.jdbcURL").getString(),
            username = config.property("storage.username").getString(),
            password = config.property("storage.password").getString(),
        ),
    )

    val healthRepository = HealthRepositoryImpl(dataSource)
    val healthService = HealthService(healthRepository)

    internalRouting(meterRegistry, healthService)
}
