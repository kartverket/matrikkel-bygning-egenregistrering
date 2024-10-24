package no.kartverket.matrikkel.bygning.repositories

import no.kartverket.matrikkel.bygning.infrastructure.database.DatabaseConfig
import no.kartverket.matrikkel.bygning.infrastructure.database.createDataSource
import no.kartverket.matrikkel.bygning.infrastructure.database.runFlywayMigrations
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
                DatabaseConfig(
                    databaseUrl = postgresSQLContainer.jdbcUrl.removePrefix("jdbc:"),
                    username = postgresSQLContainer.username,
                    password = postgresSQLContainer.password,
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
