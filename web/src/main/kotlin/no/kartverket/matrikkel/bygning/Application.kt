package no.kartverket.matrikkel.bygning

import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.application.health.HealthService
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseService
import no.kartverket.matrikkel.bygning.config.loadConfiguration
import no.kartverket.matrikkel.bygning.infrastructure.database.DatabaseConfig
import no.kartverket.matrikkel.bygning.infrastructure.database.TransactionalSupport
import no.kartverket.matrikkel.bygning.infrastructure.database.createDataSource
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.EgenregistreringRepositoryImpl
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.HealthRepositoryImpl
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.HendelseRepositoryImpl
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.bygning.BygningRepositoryImpl
import no.kartverket.matrikkel.bygning.infrastructure.database.runFlywayMigrations
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.MatrikkelApiConfig
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.MatrikkelApiFactory
import no.kartverket.matrikkel.bygning.plugins.OpenApiSpecIds
import no.kartverket.matrikkel.bygning.plugins.authentication.configureAuthentication
import no.kartverket.matrikkel.bygning.plugins.configureHTTP
import no.kartverket.matrikkel.bygning.plugins.configureMonitoring
import no.kartverket.matrikkel.bygning.plugins.configureOpenAPI
import no.kartverket.matrikkel.bygning.plugins.configureStatusPages
import no.kartverket.matrikkel.bygning.routes.internalRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.eksternRouting
import no.kartverket.matrikkel.bygning.routes.v1.intern.internRouting

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

    val dataSource =
        createDataSource(
            DatabaseConfig(
                databaseUrl = config.property("storage.jdbcURL").getString(),
                username = config.property("storage.username").getString(),
                password = config.property("storage.password").getString(),
            ),
        )

    val matrikkelApiConfig =
        MatrikkelApiConfig(
            useStub = config.propertyOrNull("matrikkel.useStub")?.getString().toBoolean(),
            baseUrl = config.propertyOrNull("matrikkel.baseUrl")?.getString() ?: "",
            username = config.propertyOrNull("matrikkel.username")?.getString() ?: "",
            password = config.propertyOrNull("matrikkel.password")?.getString() ?: "",
        )
    val matrikkelApiFactory = MatrikkelApiFactory(matrikkelApiConfig)
    val bygningClient = matrikkelApiFactory.createBygningClient()
    val matrikkelAuth = matrikkelApiFactory.createAuthService()

    val egenregistreringRepository = EgenregistreringRepositoryImpl()
    val bygningRepository = BygningRepositoryImpl(dataSource)
    val hendelseRepository = HendelseRepositoryImpl(dataSource)

    val bygningService =
        BygningService(
            bygningClient = bygningClient,
            bygningRepository = bygningRepository,
        )

    val hendelseService =
        HendelseService(
            hendelseRepository = hendelseRepository,
        )

    configureAuthentication(config, matrikkelAuth)

    val egenregistreringService =
        EgenregistreringService(
            bygningService = bygningService,
            egenregistreringRepository = egenregistreringRepository,
            transactional = TransactionalSupport(dataSource),
            hendelseRepository = hendelseRepository,
            bygningRepository = bygningRepository,
        )

    routing {
        listOf(
            OpenApiSpecIds.INTERN,
            OpenApiSpecIds.BERETTIGET_INTERESSE,
            OpenApiSpecIds.UTEN_PERSONDATA,
            OpenApiSpecIds.MED_PERSONDATA,
            OpenApiSpecIds.HENDELSER,
        ).forEach { specId ->
            route(specId) {
                route("api.json") {
                    openApi(specId)
                }
            }
        }
        route(OpenApiSpecIds.INTERN) {
            route("swagger-ui") {
                swaggerUI("/${OpenApiSpecIds.INTERN}/api.json")
            }
        }
        route("swagger-ui") {
            swaggerUI(
                mapOf(
                    "Berettiget interesse" to "/${OpenApiSpecIds.BERETTIGET_INTERESSE}/api.json",
                    "Med persondata" to "/${OpenApiSpecIds.MED_PERSONDATA}/api.json",
                    "Uten persondata" to "/${OpenApiSpecIds.UTEN_PERSONDATA}/api.json",
                    "Hendelseslogg" to "/${OpenApiSpecIds.HENDELSER}/api.json",
                ),
            )
        }

        route("v1") {
            internRouting(egenregistreringService, bygningService)
            eksternRouting(bygningService, hendelseService)
        }
    }
    runFlywayMigrations(dataSource)
}

fun Application.internalModule() {
    val config = loadConfiguration(environment)

    configureMonitoring()

    val dataSource =
        createDataSource(
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
