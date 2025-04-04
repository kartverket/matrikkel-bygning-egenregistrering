package no.kartverket.matrikkel.bygning

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.DEFAULT_AUDIENCE
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.DEFAULT_ISSUER
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.MATRIKKEL_AUDIENCE
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.MATRIKKEL_ISSUER
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer

abstract class TestApplicationWithDb {
    companion object {
        private val postgresSQLContainer = PostgreSQLContainer("postgres:15-alpine")

        @JvmStatic
        protected lateinit var mockOAuthServer: MockOAuth2Server

        @BeforeAll
        @JvmStatic
        fun setUp() {
            postgresSQLContainer.withDatabaseName("bygning")
            postgresSQLContainer.start()

            mockOAuthServer = MockOAuth2Server()
            mockOAuthServer.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            postgresSQLContainer.stop()
            mockOAuthServer.shutdown()
        }

        fun ApplicationTestBuilder.mainModuleWithDatabaseEnvironmentAndClient(): HttpClient {
            setTestConfiguration()

            application {
                mainModule()
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

        fun ApplicationTestBuilder.internalModuleWithDatabaseEnvironment() {
            setTestConfiguration()

            application {
                internalModule()
            }
        }

        private fun ApplicationTestBuilder.setTestConfiguration() {
            environment {
                config =
                    MapApplicationConfig(
                        "storage.jdbcURL" to postgresSQLContainer.jdbcUrl.removePrefix("jdbc:"),
                        "storage.username" to postgresSQLContainer.username,
                        "storage.password" to postgresSQLContainer.password,
                        "matrikkel.useStub" to "true",
                        "matrikkel.oidc.issuer" to mockOAuthServer.issuerUrl(MATRIKKEL_ISSUER).toString(),
                        "matrikkel.oidc.jwksUri" to mockOAuthServer.jwksUrl(MATRIKKEL_ISSUER).toString(),
                        "matrikkel.oidc.audience" to MATRIKKEL_AUDIENCE,
                        "matrikkel.oidc.disabled" to "false",
                        "registrert_eier.useFake" to "true",
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
    }
}
