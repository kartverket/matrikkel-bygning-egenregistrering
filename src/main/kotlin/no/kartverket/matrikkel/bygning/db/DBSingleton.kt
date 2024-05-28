package no.kartverket.matrikkel.bygning.db

import java.sql.Connection
import java.sql.DriverManager
import io.ktor.server.*
import io.ktor.server.config.*

object DatabaseSingleton {
    private var connection: Connection? = null

    fun init() {
        try {
            val jdbcURL = ApplicationConfig(null).property("storage.jdbcURL").getString()

            connection = DriverManager.getConnection(jdbcURL)
            connection?.let {
                runSQLScript(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    private fun runSQLScript(connection: Connection) {
        val script = this::class.java.classLoader.getResource("init.sql")?.readText()
        script?.let {
            val statements = it.split(";")
            statements.forEach { statement ->
                if (statement.isNotBlank()) {
                    connection.createStatement().execute(statement.trim())
                }
            }
        }
    }

    fun getConnection(): Connection? {
        return connection
    }
}