package no.kartverket.matrikkel.bygning.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.flywaydb.core.Flyway
import javax.sql.DataSource

fun createDataSource(config: ApplicationConfig): DataSource {
    val hikariConfig = HikariConfig()
        .apply {
            jdbcUrl = "jdbc:${config.property("storage.jdbcURL").getString()}"
            username = config.property("storage.username").getString()
            password = config.property("storage.password").getString()
            maximumPoolSize = 10
        }
    return HikariDataSource(hikariConfig)
}

fun runFlywayMigrations(dataSource: DataSource) {
    val flyway = Flyway.configure()
        .validateMigrationNaming(true)
        .createSchemas(true)
        .defaultSchema("bygning")
        .outputQueryResults(true)
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()

    flyway.migrate()
}
