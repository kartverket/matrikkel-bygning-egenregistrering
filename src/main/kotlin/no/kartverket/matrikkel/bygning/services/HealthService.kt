package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.repositories.HealthRepository

class HealthService(private val healthRepository: HealthRepository) {
    fun getHealth(): Boolean {
        return healthRepository.getHealthCheck()
    }
}