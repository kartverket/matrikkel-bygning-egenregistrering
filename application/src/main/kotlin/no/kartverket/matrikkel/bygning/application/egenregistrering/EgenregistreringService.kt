package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import no.kartverket.matrikkel.bygning.application.bygning.BygningRepository
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.transaction.Transactional

class EgenregistreringService(
    private val bygningService: BygningService,
    private val egenregistreringRepository: EgenregistreringRepository,
    private val bygningRepository: BygningRepository,
    private val transactional: Transactional
) {
    fun addEgenregistrering(egenregistrering: Egenregistrering): Result<Unit, DomainError> {
        return bygningService.getBygningByBubbleId(egenregistrering.bygningRegistrering.bygningBubbleId.value)
            .andThen { bygning ->
                EgenregistreringValidator.validateEgenregistrering(egenregistrering, bygning).map { bygning }
            }
            .map { bygning ->
                transactional.withTransaction { tx ->
                    egenregistreringRepository.saveEgenregistrering(egenregistrering, tx)
                    bygningService.createSnapshotsOfBruksenheterWithLatestEgenregistrering(bygning, egenregistrering)
                        .let { bruksenheter -> bygningRepository.saveBruksenheter(bruksenheter, tx) }
                }
            }
    }
}
