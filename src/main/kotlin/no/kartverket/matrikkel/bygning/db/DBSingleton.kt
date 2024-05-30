package no.kartverket.matrikkel.bygning.db

import io.ktor.server.config.*
import java.sql.Connection
import java.sql.DriverManager

object DatabaseSingleton {
    private var connection: Connection? = null
    private val jdbcURL = "jdbc:${ApplicationConfig(null).property("storage.jdbcURL").getString()}"
    private val username = ApplicationConfig(null).property("storage.username").getString()
    private val password = ApplicationConfig(null).property("storage.password").getString()

    fun init() {
        try {
            connection = DriverManager.getConnection(jdbcURL, username, password)

            val searchPathStatement = connection?.prepareStatement("set search_path = 'bygning'")

            searchPathStatement?.execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getConnection(): Connection? {
        return connection
    }

    fun getJdbcURL(): String {
        return jdbcURL
    }

    fun getUsername(): String {
        return username
    }

    fun getPassword(): String {
        return password
    }
}