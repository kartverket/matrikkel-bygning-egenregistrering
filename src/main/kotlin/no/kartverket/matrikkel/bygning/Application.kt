package no.kartverket.matrikkel.bygning

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import no.kartverket.matrikkel.bygning.routes.v1.baseRoutesV1
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.kartverket.matrikkel.bygning.db.DatabaseSingleton
import no.kartverket.matrikkel.bygning.services.BygningService
import org.slf4j.event.Level

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
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

    val dbConnection = DatabaseSingleton.getConnection()

    dbConnection?.let {
        val statement = it.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM MyTable")


        while (resultSet.next()) {
            println(resultSet.getString("name"))
        }
    }

    val bygningService = BygningService()

    baseRoutesV1(bygningService)
}
