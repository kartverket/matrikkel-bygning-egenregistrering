package no.kartverket.matrikkel.bygning

import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.json.schema.KotlinXSchemaConfigurator
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.oas.serialization.KompendiumSerializersModule
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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.db.DatabaseSingleton
import no.kartverket.matrikkel.bygning.repositories.HealthRepository
import no.kartverket.matrikkel.bygning.routes.v1.baseRoutesV1
import no.kartverket.matrikkel.bygning.routes.v1.probeRouting
import no.kartverket.matrikkel.bygning.services.BygningService
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService
import no.kartverket.matrikkel.bygning.services.HealthService

val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::internalModule).start(wait = false)

    EngineMain.main(args)
}


@OptIn(ExperimentalSerializationApi::class)
fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            serializersModule = KompendiumSerializersModule.module
            encodeDefaults = true
            explicitNulls = false
        })
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

    install(NotarizedApplication()) {
        spec = OpenApiSpec(
            jsonSchemaDialect = "https://spec.openapis.org/oas/3.1/dialect/base", info = Info(
                title = "API For Egenregistrering av Bygningsdata",
                version = "0.1",
            )
        )
        schemaConfigurator = KotlinXSchemaConfigurator()
    }

    DatabaseSingleton.init()
    DatabaseSingleton.migrate()
    val dbConnection = DatabaseSingleton.getConnection()

    // Begge disse skal egentlig ha en "bygningRepository" på seg, men databasen finnes ikke ennå
    val bygningService = BygningService()
    val egenregistreringsService = EgenregistreringsService(bygningService)

    baseRoutesV1(bygningService, egenregistreringsService)
}

fun Application.internalModule() {
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }

    DatabaseSingleton.init()
    val dbConnection = DatabaseSingleton.getConnection()

    if (dbConnection != null) {
        val healthRepository = HealthRepository(dbConnection)
        val healthService = HealthService(healthRepository)

        routing {
            get("/metrics") {
                call.respondText(appMicrometerRegistry.scrape())
            }

            probeRouting(healthService)
        }
    }
}
