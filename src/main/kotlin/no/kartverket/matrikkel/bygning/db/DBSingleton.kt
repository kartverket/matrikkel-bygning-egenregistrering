package no.kartverket.matrikkel.bygning.db

import io.ktor.server.config.*
import java.sql.Connection
import java.sql.DriverManager

object DatabaseSingleton {
    private var connection: Connection? = null

    fun init() {
        try {
            val jdbcURL = ApplicationConfig(null).property("storage.jdbcURL").getString()
            val username = ApplicationConfig(null).property("storage.username").getString()
            val password = ApplicationConfig(null).property("storage.password").getString()

            connection = DriverManager.getConnection(jdbcURL, username, password)
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    fun getConnection(): Connection? {
        return connection
    }
}