package no.kartverket.matrikkel.bygning

import DatabaseConfig
import DatabaseFactory
import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.json.schema.KotlinXSchemaConfigurator
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.oas.serialization.KompendiumSerializersModule
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.repositories.BygningRepository
import no.kartverket.matrikkel.bygning.repositories.HealthRepository
import no.kartverket.matrikkel.bygning.routes.installInternalRouting
import no.kartverket.matrikkel.bygning.routes.v1.installBaseRouting
import no.kartverket.matrikkel.bygning.services.BygningService
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService
import no.kartverket.matrikkel.bygning.services.HealthService
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.KoinIsolated

val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::internalModule).start(wait = false)

    EngineMain.main(args)
}

val databaseConfig = DatabaseConfig(
    driverClassName = "org.postgresql.Driver",
    jdbcUrl = "jdbc:${ApplicationConfig(null).property("storage.jdbcURL").getString()}",
    username = ApplicationConfig(null).property("storage.username").getString(),
    password = ApplicationConfig(null).property("storage.password").getString(),
    maxPoolSize = 10
)

val databaseModule = module {
    single { databaseConfig }
    single { DatabaseFactory(get()) }
    single { get<DatabaseFactory>().getDataSource() }
}

val appModule = module {
    // Repositories
    single { BygningRepository(get()) }

    // Services
    single { BygningService(get()) }
    single { EgenregistreringsService(get()) }
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

    install(KoinIsolated) {
        modules(appModule, databaseModule)
    }

    val dbFactory: DatabaseFactory by inject()
    dbFactory.runFlywayMigrations()

    installBaseRouting()
}

val internalModule = module {
    single { HealthRepository(get()) }
    single { HealthService(get()) }
}

fun Application.internalModule() {
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }

    install(KoinIsolated) {
        modules(internalModule, databaseModule)
    }

    installInternalRouting()
}