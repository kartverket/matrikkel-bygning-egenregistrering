package no.kartverket.matrikkel.bygning

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.infrastructure.database.DatabaseConfig
import no.kartverket.matrikkel.bygning.infrastructure.database.createDataSource
import no.kartverket.matrikkel.bygning.infrastructure.database.runFlywayMigrations
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.DEFAULT_AUDIENCE
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.DEFAULT_ISSUER
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

abstract class TestApplicationWithDb {

    @AfterEach
    fun clearData() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    DELETE FROM bygning.bruksenhet;
                    DELETE FROM bygning.egenregistrering;
                    DELETE FROM bygning.hendelse;
                    """,
                )
            }
        }
    }

    companion object {
        private val postgresSQLContainer = PostgreSQLContainer("postgres:15-alpine")

        @JvmStatic
        protected lateinit var mockOAuthServer: MockOAuth2Server

        lateinit var dataSource: DataSource

        @BeforeAll
        @JvmStatic
        fun setUp() {
            postgresSQLContainer.withDatabaseName("bygning")
            postgresSQLContainer.start()
            dataSource = createDataSource(
                DatabaseConfig(
                    databaseUrl = postgresSQLContainer.jdbcUrl.removePrefix("jdbc:"),
                    username = postgresSQLContainer.username,
                    password = postgresSQLContainer.password,
                ),
            )
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

            runFlywayMigrations(dataSource)
        }

        private fun ApplicationTestBuilder.setTestConfiguration() {
            environment {
                config = MapApplicationConfig(
                    "storage.jdbcURL" to postgresSQLContainer.jdbcUrl.removePrefix("jdbc:"),
                    "storage.username" to postgresSQLContainer.username,
                    "storage.password" to postgresSQLContainer.password,
                    "matrikkel.useStub" to "true",
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
