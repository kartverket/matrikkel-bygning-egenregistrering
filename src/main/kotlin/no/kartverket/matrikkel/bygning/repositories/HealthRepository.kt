package no.kartverket.matrikkel.bygning.repositories

import no.kartverket.matrikkel.bygning.db.executeQueryAndMapPreparedStatement
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class HealthRepository(private val dataSource: DataSource) {
    fun getHealthCheck(): Boolean {
        @Language("PostgreSQL")
        val sql = "SELECT 1 WHERE 1 = ?;"

        val resultSet = dataSource.executeQueryAndMapPreparedStatement(
            sql,
            { it.setInt(1, 1) },
        ) { res -> res.getInt(1) }

        return resultSet.isNotEmpty()
    }
}
