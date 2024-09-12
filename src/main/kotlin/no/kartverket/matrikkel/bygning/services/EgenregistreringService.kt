package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.models.Result
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.repositories.EgenregistreringRepository

class EgenregistreringService(
    private val bygningClient: BygningClient, private val egenregistreringRepository: EgenregistreringRepository
) {
    fun addEgenregistrering(egenregistrering: Egenregistrering): Result<Unit> {
        val bygning = bygningClient.getBygningById(egenregistrering.bygningRegistrering.bygningId) ?: return Result.ErrorResult(
            ErrorDetail(
                detail = "Bygning med ID ${egenregistrering.bygningRegistrering.bygningId} finnes ikke i matrikkelen",
            ),
        )

        val invalidBruksenheter = findBruksenheterNotRegisteredOnCorrectBygning(egenregistrering, bygning)
        if (invalidBruksenheter.isNotEmpty()) {
            return Result.ErrorResult(
                ErrorDetail(
                    detail = "Bruksenhet${if (invalidBruksenheter.size > 1) "er" else ""} med ID ${invalidBruksenheter.joinToString()} finnes ikke i bygning med ID ${bygning.bygningId}",
                ),
            )
        }

        return egenregistreringRepository.saveEgenregistrering(egenregistrering);
    }

    private fun findBruksenheterNotRegisteredOnCorrectBygning(
        egenregistrering: Egenregistrering, bygning: Bygning
    ): List<Long> {
        return egenregistrering.bygningRegistrering.bruksenhetRegistreringer.mapNotNull { bruksenhetRegistering ->
            val bruksenhet = bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId }

            if (bruksenhet == null) {
                bruksenhetRegistering.bruksenhetId
            } else {
                null
            }
        }
    }

    fun findAllEgenregistreringerForBygning(bygningId: Long): List<Egenregistrering> {
        return egenregistreringRepository.getAllEgenregistreringerForBygning(bygningId)
    }

    fun findNewestEgenregistreringForBygning(bygningId: Long): Egenregistrering? {
        return findAllEgenregistreringerForBygning(bygningId).firstOrNull()
    }
}
