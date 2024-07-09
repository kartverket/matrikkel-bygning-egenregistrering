package no.kartverket.matrikkel.bygning.db

import io.ktor.server.config.*
import io.ktor.util.logging.*
import org.flywaydb.core.Flyway
import java.sql.Connection
import java.sql.DriverManager

val LOGGER = KtorSimpleLogger("no.kartverket.matrikkel.bygning.db.DatabaseSingleton")

object DatabaseSingleton {
    private var connection: Connection? = null
    private val jdbcURL = "jdbc:${ApplicationConfig(null).property("storage.jdbcURL").getString()}"
    private val username = ApplicationConfig(null).property("storage.username").getString()
    private val password = ApplicationConfig(null).property("storage.password").getString()

    fun init() {
        try {
            connection = DriverManager.getConnection(jdbcURL, username, password)
        } catch (e: Exception) {
            LOGGER.error(e.stackTraceToString())
        }
    }

    fun migrate() {
        try {
            val flyway = Flyway.configure().validateMigrationNaming(true).createSchemas(true).defaultSchema("bygning").outputQueryResults(true)
                .dataSource(
                    jdbcURL, username, password
                ).load()

            flyway.migrate()

            val searchPathStatement = connection?.prepareStatement("set search_path = 'bygning'")
            searchPathStatement?.execute()
        } catch (e: Exception) {
            LOGGER.error(e.stackTraceToString())
        }
    }

    fun getConnection(): Connection {
        if (connection != null) {
            return connection as Connection
        }

        throw RuntimeException("Kunne ikke koble til database")
    }
}