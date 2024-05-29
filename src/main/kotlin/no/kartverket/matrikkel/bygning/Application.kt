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
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.kartverket.matrikkel.bygning.db.DatabaseSingleton
import no.kartverket.matrikkel.bygning.repositories.BygningRepository
import no.kartverket.matrikkel.bygning.routes.v1.baseRoutesV1
import no.kartverket.matrikkel.bygning.services.BygningService
import org.flywaydb.core.Flyway
import org.slf4j.event.Level

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
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/v1") }
    }

    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }

    routing {
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }

    DatabaseSingleton.init()

    val dbConnection = DatabaseSingleton.getConnection() ?: throw RuntimeException("Kunne ikke koble til database")

    val url = environment.config.property("storage.jdbcURL").getString()
    val username = environment.config.property("storage.username").getString()
    val password = environment.config.property("storage.password").getString()

    val flyway = Flyway.configure().validateMigrationNaming(true).dataSource(
        url, username, password
    ).load()

    flyway.migrate()

    val bygningRepository = BygningRepository(dbConnection)

    val bygningService = BygningService(bygningRepository)

    baseRoutesV1(bygningService)
}
