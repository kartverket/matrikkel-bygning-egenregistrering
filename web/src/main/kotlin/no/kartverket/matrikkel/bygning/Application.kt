package no.kartverket.matrikkel.bygning

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
import no.kartverket.matrikkel.bygning.config.loadConfiguration
import no.kartverket.matrikkel.bygning.infrastructure.database.DatabaseConfig
import no.kartverket.matrikkel.bygning.infrastructure.database.createDataSource
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.EgenregistreringRepositoryImpl
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.HealthRepositoryImpl
import no.kartverket.matrikkel.bygning.infrastructure.database.runFlywayMigrations
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.MatrikkelApiConfig
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.createBygningClient
import no.kartverket.matrikkel.bygning.plugins.DigDirAuthenticationConfig
import no.kartverket.matrikkel.bygning.plugins.JWTAuthenticationConfig
import no.kartverket.matrikkel.bygning.plugins.configureDigDirAuthentication
import no.kartverket.matrikkel.bygning.plugins.configureHTTP
import no.kartverket.matrikkel.bygning.plugins.configureMonitoring
import no.kartverket.matrikkel.bygning.plugins.configureOpenAPI
import no.kartverket.matrikkel.bygning.plugins.configureStatusPages
import no.kartverket.matrikkel.bygning.routes.internalRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.eksternRouting
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.bygningRouting
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.egenregistreringRouting
import no.kartverket.matrikkel.bygning.routes.v1.kodeliste.kodelisteRouting

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
    configureDigDirAuthentication(
        DigDirAuthenticationConfig(
            maskinporten = JWTAuthenticationConfig(
                name = "maskinporten",
                jwksUri = config.property("maskinporten.jwksUri").getString(),
                issuer = config.property("maskinporten.issuer").getString(),
                scope = config.property("maskinporten.scopes").getString(),
                shouldSkip = config.propertyOrNull("maskinporten.shouldSkip")?.getString().toBoolean(),
            ),
            idporten = JWTAuthenticationConfig(
                name = "idporten",
                jwksUri = config.property("idporten.jwksUri").getString(),
                issuer = config.property("idporten.issuer").getString(),
                scope = config.property("idporten.scopes").getString(),
                shouldSkip = config.propertyOrNull("idporten.shouldSkip")?.getString().toBoolean(),
            ),
        ),
    )

    val dataSource = createDataSource(
        DatabaseConfig(
            databaseUrl = config.property("storage.jdbcURL").getString(),
            username = config.property("storage.username").getString(),
            password = config.property("storage.password").getString(),
        ),
    )

    val egenregistreringRepository = EgenregistreringRepositoryImpl(dataSource)

    val bygningClient = createBygningClient(
        MatrikkelApiConfig(
            useStub = config.property("matrikkel.useStub").getString().toBoolean(),
            baseUrl = config.propertyOrNull("matrikkel.baseUrl")?.getString() ?: "",
            username = config.propertyOrNull("matrikkel.username")?.getString() ?: "",
            password = config.propertyOrNull("matrikkel.password")?.getString() ?: "",
        ),
    )


    val egenregistreringService = EgenregistreringService(bygningClient, egenregistreringRepository)
    val bygningService = BygningService(bygningClient, egenregistreringService)

    routing {
        route("api.json") {
            openApiSpec()
        }
        route("swagger-ui") {
            swaggerUI("/api.json")
        }

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
