package no.kartverket.matrikkel.bygning.repositories

import javax.sql.DataSource

class HealthRepository(private val dataSource: DataSource) {
    fun getHealthCheck(): Boolean {
        val sql = "select 1;"

        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                return statement.executeQuery(sql).next()
            }
        }
    }
}
