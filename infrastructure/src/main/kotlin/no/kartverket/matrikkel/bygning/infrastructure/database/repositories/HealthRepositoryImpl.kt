package no.kartverket.matrikkel.bygning.infrastructure.database.repositories

import kotliquery.sessionOf
import no.kartverket.matrikkel.bygning.application.health.HealthRepository
import javax.sql.DataSource

class HealthRepositoryImpl(private val dataSource: DataSource) : HealthRepository{
    override fun isHealthy(): Boolean {
        return sessionOf(dataSource).use { it.connection.underlying.isValid(0) }
    }
}
