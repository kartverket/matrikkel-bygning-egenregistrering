package no.kartverket.matrikkel.bygning.application.health

interface HealthRepository {
    fun isHealthy(): Boolean
}
