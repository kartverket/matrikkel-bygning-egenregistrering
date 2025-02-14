package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import no.kartverket.matrikkel.bygning.application.bygning.BygningRepository
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.hendelser.HendelsePayload.BruksenhetOppdatertPayload
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseRepository
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.applyEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.transaction.Transactional

class EgenregistreringService(
    private val bygningService: BygningService,
    private val egenregistreringRepository: EgenregistreringRepository,
    private val hendelseRepository: HendelseRepository,
    private val bygningRepository: BygningRepository,
    private val transactional: Transactional,
) {
    fun addEgenregistrering(egenregistrering: Egenregistrering): Result<Unit, DomainError> {
        return bygningService.getBygningByBubbleId(egenregistrering.bygningRegistrering.bygningBubbleId.value).andThen { bygning ->
            EgenregistreringValidator.validateEgenregistrering(egenregistrering, bygning).map { bygning }
        }.map { bygning ->
            transactional.withTransaction { tx ->
                egenregistreringRepository.saveEgenregistrering(egenregistrering, tx)

                bygningRepository.saveBruksenheter(
                    bruksenheter = createBruksenhetSnapshotsOfLatestEgenregistrering(bygning, egenregistrering),
                    registreringstidspunkt = egenregistrering.registreringstidspunkt,
                    tx = tx,
                )

                hendelseRepository.saveHendelser(
                    payloads = createEgenregistreringHendelsePayloads(egenregistrering),
                    tx = tx,
                )
            }
        }
    }

    private fun createBruksenhetSnapshotsOfLatestEgenregistrering(
        bygning: Bygning, egenregistrering: Egenregistrering
    ): List<Bruksenhet> {
        return egenregistrering.bygningRegistrering.bruksenhetRegistreringer.mapNotNull { bruksenhetRegistrering ->
            bygning.bruksenheter.find { bruksenhet -> bruksenhet.bruksenhetBubbleId == bruksenhetRegistrering.bruksenhetBubbleId }
                ?.applyEgenregistrering(egenregistrering)
        }
    }

    private fun createEgenregistreringHendelsePayloads(egenregistrering: Egenregistrering): List<BruksenhetOppdatertPayload> {
        return egenregistrering.bygningRegistrering.bruksenhetRegistreringer.map { registrering ->
            BruksenhetOppdatertPayload(
                objectId = registrering.bruksenhetBubbleId.value,
                registreringstidspunkt = egenregistrering.registreringstidspunkt,
            )
        }
    }
}
