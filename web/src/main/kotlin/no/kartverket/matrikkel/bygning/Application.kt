package no.kartverket.matrikkel.bygning

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.SwaggerUIData
import io.github.smiley4.ktorswaggerui.data.SwaggerUiSort
import io.github.smiley4.ktorswaggerui.data.SwaggerUiSyntaxHighlight
import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.github.smiley4.ktorswaggerui.routing.ApiSpec
import io.github.smiley4.ktorswaggerui.routing.ResourceContent
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.application.health.HealthService
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseService
import no.kartverket.matrikkel.bygning.config.Env
import no.kartverket.matrikkel.bygning.config.loadConfiguration
import no.kartverket.matrikkel.bygning.infrastructure.database.DatabaseConfig
import no.kartverket.matrikkel.bygning.infrastructure.database.TransactionalSupport
import no.kartverket.matrikkel.bygning.infrastructure.database.createDataSource
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.EgenregistreringRepositoryImpl
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.HealthRepositoryImpl
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.HendelseRepositoryImpl
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

    val bygningClient = if (Env.isLocal() && config.property("matrikkel.useStub").getString().toBoolean()) {
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

    val egenregistreringRepository = EgenregistreringRepositoryImpl()
    val bygningRepository = BygningRepositoryImpl(dataSource)
    val hendelseRepository = HendelseRepositoryImpl(dataSource)

    val bygningService = BygningService(
        bygningClient = bygningClient,
        bygningRepository = bygningRepository,
    )

    val hendelseService = HendelseService(
        hendelseRepository = hendelseRepository,
    )

    val egenregistreringService = EgenregistreringService(
        bygningService = bygningService,
        egenregistreringRepository = egenregistreringRepository,
        transactional = TransactionalSupport(dataSource),
        hendelseRepository = hendelseRepository,
        bygningRepository = bygningRepository,
    )

    routing {
        // OpenAPI / Swagger for interne routes
        route("intern") {
            route("api.json") {
                openApiSpec(OpenApiSpecIds.INTERN)
            }
            route("swagger-ui") {
                swaggerUI("/intern/api.json")
            }
        }
        route("hendelser") {
            // OpenAPI / Swagger for eksterne routes
            route("api.json") {
                openApiSpec(OpenApiSpecIds.HENDELSER)
            }
        }
        route("medpersondata") {
            // OpenAPI / Swagger for eksterne routes
            route("api.json") {
                openApiSpec(OpenApiSpecIds.MED_PERSONDATA)
            }
            route("swagger-ui") {
                swaggerUI("/medpersondata/api.json")
            }
        }
        route("swagger-ui") {
            swaggerUIs(
                "Med persondata" to "/medpersondata/api.json",
                "Hendelser" to "/hendelser/api.json",
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

fun Route.swaggerUIs(vararg apis: Pair<String, String>) {
    route({ hidden = true }) {
        get {
            call.respondRedirect("${call.request.uri}/index.html")
        }
        get("{filename}") {
            serveStaticResource(call.parameters["filename"]!!, "5.17.11", call) // Denne versjonen må stemme med hva ktor-swagger-ui bruker
        }
        get("swagger-initializer.js") {
            serveSwaggerInitializer(call, ApiSpec.swaggerUiConfig, apis.asIterable())
        }
    }
}

private suspend fun serveSwaggerInitializer(call: ApplicationCall, swaggerUiConfig: SwaggerUIData, apis: Iterable<Pair<String, String>>) {
    // see https://github.com/swagger-api/swagger-ui/blob/master/docs/usage/configuration.md for reference
    val propValidatorUrl = swaggerUiConfig.validatorUrl?.let { "validatorUrl: \"$it\"" } ?: "validatorUrl: false"
    val propDisplayOperationId = "displayOperationId: ${swaggerUiConfig.displayOperationId}"
    val propFilter = "filter: ${swaggerUiConfig.showTagFilterInput}"
    val propSort = "operationsSorter: " +
        if (swaggerUiConfig.sort == SwaggerUiSort.NONE) "undefined"
        else "\"${swaggerUiConfig.sort.value}\""
    val propSyntaxHighlight = "syntaxHighlight: " +
        if(swaggerUiConfig.syntaxHighlight == SwaggerUiSyntaxHighlight.DISABLED) "false"
        else "{ theme: \"${swaggerUiConfig.syntaxHighlight.value}\" }"
    val content = """
			window.onload = function() {
			  window.ui = SwaggerUIBundle({
				urls: [${apis.joinToString(separator = ",", transform = { "{ url: \"${it.second}\", name: \"${it.first}\" }" })}],
				dom_id: '#swagger-ui',
				deepLinking: true,
				presets: [
				  SwaggerUIBundle.presets.apis,
				  SwaggerUIStandalonePreset
				],
				plugins: [
				  SwaggerUIBundle.plugins.DownloadUrl
				],
				layout: "StandaloneLayout",
				withCredentials: ${swaggerUiConfig.withCredentials},
				$propValidatorUrl,
  				$propDisplayOperationId,
    		    $propFilter,
    		    $propSort,
				$propSyntaxHighlight
			  });
			};
		""".trimIndent()
    call.respondText(ContentType.Application.JavaScript, HttpStatusCode.OK) { content }
}

private suspend fun serveStaticResource(filename: String, swaggerWebjarVersion: String, call: ApplicationCall) {
    val resourceName = "/META-INF/resources/webjars/swagger-ui/$swaggerWebjarVersion/$filename"
    val resource = SwaggerUI::class.java.getResource(resourceName)
    if (resource != null) {
        call.respond(ResourceContent(resource))
    } else {
        call.respond(HttpStatusCode.NotFound, "$filename could not be found")
    }
}
