package no.kartverket.matrikkel.bygning

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.mockk.mockk
import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseService
import no.kartverket.matrikkel.bygning.config.loadConfiguration
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.MatrikkelApiConfig
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.MatrikkelApiFactory
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.DEFAULT_AUDIENCE
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.DEFAULT_ISSUER
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.MATRIKKEL_AUDIENCE
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.MATRIKKEL_ISSUER
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

abstract class TestApplicationWithFakes {
    companion object {
        @JvmStatic
        protected lateinit var mockOAuthServer: MockOAuth2Server

        @BeforeAll
        @JvmStatic
        fun setUp() {
            mockOAuthServer = MockOAuth2Server()
            mockOAuthServer.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            mockOAuthServer.shutdown()
        }
    }

    fun ApplicationTestBuilder.mainModuleWithClient(
        routingConfigurator: (RoutingConfig.() -> Unit)? = null
    ): HttpClient {
        setTestConfiguration()

        application {
            val config = loadConfiguration(environment)

            val matrikkelApiFactory = MatrikkelApiFactory(MatrikkelApiConfig.forStubbing())

            val routingConfig = RoutingConfig()
            if (routingConfigurator != null) {
                routingConfig.routingConfigurator()
            }

            configureRouting(
                config,
                matrikkelApiFactory.createAuthService(),
                routingConfig.egenregistreringService ?: mockk(),
                routingConfig.bygningService ?: mockk(),
                routingConfig.hendelseService ?: mockk(),
            )
        }

        return createClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        explicitNulls = false
                    },
                )
            }
        }
    }

    private fun ApplicationTestBuilder.setTestConfiguration() {
        environment {
            config = MapApplicationConfig(
                "storage.jdbcURL" to "<not used>",
                "matrikkel.useStub" to "true",
                "matrikkel.oidc.issuer" to mockOAuthServer.issuerUrl(MATRIKKEL_ISSUER).toString(),
                "matrikkel.oidc.jwksUri" to mockOAuthServer.jwksUrl(MATRIKKEL_ISSUER).toString(),
                "matrikkel.oidc.audience" to MATRIKKEL_AUDIENCE,
                "matrikkel.oidc.disabled" to "false",
                "maskinporten.issuer" to mockOAuthServer.issuerUrl(DEFAULT_ISSUER).toString(),
                "maskinporten.jwksUri" to mockOAuthServer.jwksUrl(DEFAULT_ISSUER).toString(),
                "maskinporten.scopes" to "kartverk:riktig:scope",
                "maskinporten.disabled" to "false",
                "idporten.issuer" to mockOAuthServer.issuerUrl(DEFAULT_ISSUER).toString(),
                "idporten.jwksUri" to mockOAuthServer.jwksUrl(DEFAULT_ISSUER).toString(),
                "idporten.disabled" to "false",
                "entra.audience" to DEFAULT_AUDIENCE,
                "entra.issuer" to mockOAuthServer.issuerUrl(DEFAULT_ISSUER).toString(),
                "entra.jwksUri" to mockOAuthServer.jwksUrl(DEFAULT_ISSUER).toString(),
                "entra.disabled" to "false",
            )
        }
    }

    class RoutingConfig {
        var egenregistreringService: EgenregistreringService? = null
        var bygningService: BygningService? = null
        var hendelseService: HendelseService? = null
    }
}
