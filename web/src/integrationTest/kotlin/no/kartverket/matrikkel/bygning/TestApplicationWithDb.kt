package no.kartverket.matrikkel.bygning

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer

abstract class TestApplicationWithDb {
    companion object {
        private val postgresSQLContainer = PostgreSQLContainer("postgres:15-alpine")

        @BeforeAll
        @JvmStatic
        fun setUp() {
            postgresSQLContainer.withDatabaseName("bygning")
            postgresSQLContainer.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            postgresSQLContainer.stop()
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun ApplicationTestBuilder.mainModuleWithDatabaseEnvironmentAndClient(): HttpClient {
            setDatabaseConfiguration()

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
            setDatabaseConfiguration()

            application {
                internalModule()
            }
        }

        private fun ApplicationTestBuilder.setDatabaseConfiguration() {
            environment {
                config = MapApplicationConfig(
                    "storage.jdbcURL" to postgresSQLContainer.jdbcUrl.removePrefix("jdbc:"),
                    "storage.username" to postgresSQLContainer.username,
                    "storage.password" to postgresSQLContainer.password,
                    "matrikkel.useStub" to "true",
                )
            }
        }
    }
}
