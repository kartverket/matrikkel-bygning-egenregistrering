package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.DomainError

class EgenregistreringService(
    private val bygningService: BygningService,
    private val egenregistreringRepository: EgenregistreringRepository,
) {
    fun addEgenregistrering(egenregistrering: Egenregistrering): Result<Unit, DomainError> {
        return bygningService.getBygningById(egenregistrering.bygningRegistrering.bygningId)
            .andThen { bygning ->
                EgenregistreringValidator.validateEgenregistrering(egenregistrering, bygning).map { bygning }
            }
            .map { bygning ->
                egenregistreringRepository.saveEgenregistrering(egenregistrering)
                bygningService.createBruksenhetSnapshotsOfEgenregistrering(bygning, egenregistrering)
            }
    }
}
