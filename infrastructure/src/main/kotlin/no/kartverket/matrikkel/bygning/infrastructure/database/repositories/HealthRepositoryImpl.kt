package no.kartverket.matrikkel.bygning.infrastructure.database.repositories

import no.kartverket.matrikkel.bygning.application.health.HealthRepository
import javax.sql.DataSource

class HealthRepositoryImpl(private val dataSource: DataSource) : HealthRepository{
    override fun isHealthy(): Boolean {
        return dataSource.connection.isValid(0)
    }
}
