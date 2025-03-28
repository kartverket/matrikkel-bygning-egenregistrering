package no.kartverket.matrikkel.bygning.application.health

class HealthService(
    private val healthRepository: HealthRepository,
) {
    fun isHealthy(): Boolean = healthRepository.isHealthy()
}
