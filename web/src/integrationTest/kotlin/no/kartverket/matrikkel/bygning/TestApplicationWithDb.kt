package no.kartverket.matrikkel.bygning

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer

abstract class TestApplicationWithDb {
    companion object {
        private val postgresSQLContainer = PostgreSQLContainer("postgres:15-alpine")
        @JvmStatic
        protected lateinit var mockOAuthServer : MockOAuth2Server

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
                config = MapApplicationConfig(
                    "storage.jdbcURL" to postgresSQLContainer.jdbcUrl.removePrefix("jdbc:"),
                    "storage.username" to postgresSQLContainer.username,
                    "storage.password" to postgresSQLContainer.password,
                    "matrikkel.useStub" to "true",
                    "maskinporten.issuer" to mockOAuthServer.issuerUrl("testIssuer").toString(),
                    "maskinporten.jwksUri" to mockOAuthServer.jwksUrl("testIssuer").toString(),
                    "maskinporten.scopes" to "kartverk:riktig:scope",
                    "maskinporten.disabled" to "false",
                    "idporten.issuer" to mockOAuthServer.issuerUrl("testIssuer").toString(),
                    "idporten.jwksUri" to mockOAuthServer.jwksUrl("testIssuer").toString(),
                    "idporten.disabled" to "false"
                )
            }
        }
    }
}
