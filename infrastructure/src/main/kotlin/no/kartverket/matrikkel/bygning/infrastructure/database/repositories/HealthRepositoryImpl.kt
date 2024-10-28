package no.kartverket.matrikkel.bygning.infrastructure.database.repositories

import no.kartverket.matrikkel.bygning.application.health.HealthRepository
import no.kartverket.matrikkel.bygning.infrastructure.database.executeQueryAndMapPreparedStatement
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class HealthRepositoryImpl(private val dataSource: DataSource) : HealthRepository{
    override fun isHealthy(): Boolean {
        @Language("PostgreSQL")
        val sql = "SELECT 1 WHERE 1 = ?;"

        val resultSet = dataSource.executeQueryAndMapPreparedStatement(
            sql,
            { it.setInt(1, 1) },
        ) { res -> res.getInt(1) }

        return resultSet?.isNotEmpty() ?: false
    }
}
