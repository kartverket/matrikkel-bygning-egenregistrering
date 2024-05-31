package no.kartverket.matrikkel.bygning.repositories

import java.sql.Connection

class HealthRepository(private val dbConnection: Connection) {
    fun getHealthCheck(): Boolean {
        val statement = dbConnection.createStatement()

        val resultSet = statement.executeQuery(
            """
            select 1;
        """.trimIndent()
        )

        return resultSet.next()
    }
}
