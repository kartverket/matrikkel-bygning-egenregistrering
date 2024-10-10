package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
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

        val validationErrors = EgenregistreringValidator.validateEgenregistrering(egenregistrering, bygning)
        if (validationErrors.isNotEmpty()) {
            return Result.ErrorResult(
                errors = validationErrors,
            )
        }

        return egenregistreringRepository.saveEgenregistrering(egenregistrering)
    }


    fun findAllEgenregistreringerForBygning(bygningId: Long): List<Egenregistrering> {
        return egenregistreringRepository.getAllEgenregistreringerForBygning(bygningId)
    }
}
