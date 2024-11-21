package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.onSuccess
import no.kartverket.matrikkel.bygning.application.bygning.BygningClient
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.DomainError

class EgenregistreringService(
    private val bygningClient: BygningClient,
    private val egenregistreringRepository: EgenregistreringRepository,
) {
    fun addEgenregistrering(egenregistrering: Egenregistrering): Result<Unit, DomainError> {
        return bygningClient
            .getBygningById(egenregistrering.bygningRegistrering.bygningId)
            .andThen {
                EgenregistreringValidator.validateEgenregistrering(egenregistrering, it)
            }
            .onSuccess {
                egenregistreringRepository.saveEgenregistrering(egenregistrering)
            }
    }

    fun findAllEgenregistreringerForBygning(bygningId: Long): List<Egenregistrering> {
        return egenregistreringRepository.getAllEgenregistreringerForBygning(bygningId)
    }
}
