package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import no.kartverket.matrikkel.bygning.application.bygning.BygningRepository
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.hendelser.HendelsePayload.BruksenhetOppdatertPayload
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseRepository
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.aggregering.applyEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.transaction.Transactional

class EgenregistreringService(
    private val bygningService: BygningService,
    private val egenregistreringRepository: EgenregistreringRepository,
    private val hendelseRepository: HendelseRepository,
    private val bygningRepository: BygningRepository,
    private val transactional: Transactional,
) {
    fun addEgenregistrering(egenregistrering: Egenregistrering): Result<Unit, DomainError> =
        bygningService
            .getBruksenhetByBubbleId(
                bruksenhetBubbleId = egenregistrering.bruksenhetRegistrering.bruksenhetBubbleId.value,
            ).andThen { bruksenhet ->
                EgenregistreringValidator.validateEgenregistrering(egenregistrering).map { bruksenhet }
            }.map { bruksenhet ->
                transactional.withTransaction { tx ->
                    egenregistreringRepository.saveEgenregistrering(
                        egenregistrering = egenregistrering,
                        tx = tx,
                    )

                    bygningRepository.saveBruksenhet(
                        bruksenhet = createBruksenhetSnapshotOfLatestEgenregistrering(bruksenhet, egenregistrering),
                        registreringstidspunkt = egenregistrering.registreringstidspunkt,
                        tx = tx,
                    )

                    hendelseRepository.saveHendelse(
                        payload = createEgenregistreringHendelsePayloads(egenregistrering),
                        tx = tx,
                    )
                }
            }

    private fun createBruksenhetSnapshotOfLatestEgenregistrering(
        bruksenhet: Bruksenhet,
        egenregistrering: Egenregistrering,
    ): Bruksenhet = bruksenhet.applyEgenregistrering(egenregistrering)

    private fun createEgenregistreringHendelsePayloads(egenregistrering: Egenregistrering): BruksenhetOppdatertPayload =
        BruksenhetOppdatertPayload(
            objectId = egenregistrering.bruksenhetRegistrering.bruksenhetBubbleId.value,
            registreringstidspunkt = egenregistrering.registreringstidspunkt,
        )
}
