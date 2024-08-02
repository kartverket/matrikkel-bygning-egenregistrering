package no.kartverket.matrikkel.bygning

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
import no.kartverket.matrikkel.bygning.db.DatabaseConfig
import no.kartverket.matrikkel.bygning.db.createHikariDataSource
import no.kartverket.matrikkel.bygning.db.runFlywayMigrations
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import no.kartverket.matrikkel.bygning.repositories.BygningRepository
import no.kartverket.matrikkel.bygning.repositories.HealthRepository
import no.kartverket.matrikkel.bygning.routes.installInternalRouting
import no.kartverket.matrikkel.bygning.routes.v1.installBaseRouting
import no.kartverket.matrikkel.bygning.services.BygningService
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService
import no.kartverket.matrikkel.bygning.services.HealthService
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.KoinIsolated
import java.net.URI
import javax.sql.DataSource

fun main() {
    embeddedServer(factory = Netty, port = 8081, module = Application::internalModule).start(wait = false)
    embeddedServer(factory = Netty, port = 8080, module = Application::mainModule).start(wait = true)
}

val databaseModule = { config: ApplicationConfig ->
    module {
        single<DataSource> { createHikariDataSource(
            DatabaseConfig(
                driverClassName = "org.postgresql.Driver",
                jdbcUrl = "jdbc:${config.property("storage.jdbcURL").getString()}",
                username = config.property("storage.username").getString(),
                password = config.property("storage.password").getString(),
                maxPoolSize = 10
            )
        ) }
    }
}

val appModule = module {
    // Repositories
    single { BygningRepository(get()) }

    // Services
    single { BygningService(get()) }
    single { EgenregistreringsService(get()) }
}

val metricsModule = module {
    single { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
}

val matrikkelModule = { config: ApplicationConfig ->
    module {
        single {
            MatrikkelApi(URI(config.property("matrikkel.baseUrl").getString()))
                .withAuth(
                    config.property("matrikkel.username").getString(),
                    config.property("matrikkel.password").getString()
                )
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun Application.mainModule() {
    val config = loadConfig()

    install(KoinIsolated) {
        modules(appModule, databaseModule(config), matrikkelModule(config), metricsModule)
    }

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

    install(NotarizedApplication()) {
        spec = OpenApiSpec(
            jsonSchemaDialect = "https://spec.openapis.org/oas/3.1/dialect/base", info = Info(
                title = "API For Egenregistrering av Bygningsdata",
                version = "0.1",
            )
        )
        schemaConfigurator = KotlinXSchemaConfigurator()
    }


    val meterRegistry = get<PrometheusMeterRegistry>()
    install(MicrometerMetrics) {
        registry = meterRegistry
    }

    runFlywayMigrations(get())

    installBaseRouting(
        matrikkelApi = get<MatrikkelApi.WithAuth>(),
        bygningService = get<BygningService>(),
        egenregistreringsService = get<EgenregistreringsService>()
    )
}

val internalModule = module {
    single { HealthRepository(get()) }
    single { HealthService(get()) }
}

fun Application.internalModule() {
    val config = loadConfig()

    install(KoinIsolated) {
        modules(internalModule, databaseModule(config), metricsModule)
    }

    val meterRegistry = get<PrometheusMeterRegistry>()
    install(MicrometerMetrics) {
        registry = meterRegistry
    }

    installInternalRouting(
        meterRegistry = meterRegistry,
        healthService = get<HealthService>(),
    )
}

private fun Application.loadConfig() =
    // Any properties set in tests or similar will be used first, then fall back to config from application.conf
    environment.config.withFallback(ApplicationConfig("application.conf"))
