package no.kartverket.matrikkel.bygning.infrastructure.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

data class DatabaseConfig(
    private val databaseUrl: String,
    val username: String,
    val password: String,
) {
    val jdbcUrl: String
        get() = "jdbc:${this.databaseUrl}"
}

fun createDataSource(config: DatabaseConfig): DataSource {
    val hikariConfig =
        HikariConfig()
            .apply {
                jdbcUrl = config.jdbcUrl
                username = config.username
                password = config.password
                maximumPoolSize = 10
            }
    return HikariDataSource(hikariConfig)
}

fun runFlywayMigrations(dataSource: DataSource) {
    val flyway =
        Flyway
            .configure()
            .validateMigrationNaming(true)
            .createSchemas(true)
            .defaultSchema("bygning")
            .outputQueryResults(true)
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()

    flyway.migrate()
}
