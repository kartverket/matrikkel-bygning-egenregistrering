package no.kartverket.matrikkel.bygning.repositories

import io.ktor.server.config.*
import no.kartverket.matrikkel.bygning.db.createDataSource
import no.kartverket.matrikkel.bygning.db.runFlywayMigrations
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

abstract class TestWithDb {
    companion object {
        private val postgresSQLContainer = PostgreSQLContainer("postgres:15-alpine")

        lateinit var dataSource: DataSource

        @BeforeAll
        @JvmStatic
        fun setUp() {
            postgresSQLContainer.withDatabaseName("bygning")
            postgresSQLContainer.start()
            dataSource = createDataSource(
                MapApplicationConfig(
                    "storage.jdbcURL" to postgresSQLContainer.jdbcUrl.removePrefix("jdbc:"),
                    "storage.username" to postgresSQLContainer.username,
                    "storage.password" to postgresSQLContainer.password,
                    "matrikkel.useStub" to "true",
                ),
            )
            runFlywayMigrations(dataSource)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            postgresSQLContainer.stop()
        }
    }
}
