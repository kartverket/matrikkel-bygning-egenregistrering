package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.toResultOr
import no.kartverket.matrikkel.bygning.application.bygning.BygningRepository
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.hendelser.HendelsePayload.BruksenhetOppdatertPayload
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseRepository
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.applyEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.applyEgenregistreringer
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.error.EgenregistreringNotFound
import no.kartverket.matrikkel.bygning.application.models.ids.EgenregistreringId
import no.kartverket.matrikkel.bygning.application.transaction.Transactional
import java.time.Instant

class EgenregistreringService(
    private val bygningService: BygningService,
    private val egenregistreringRepository: EgenregistreringRepository,
    private val hendelseRepository: HendelseRepository,
    private val bygningRepository: BygningRepository,
    private val transactional: Transactional,
) {
    fun addEgenregistrering(egenregistrering: Egenregistrering): Result<Unit, DomainError> {
        return bygningService.getBruksenhetByBubbleId(
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
    }

    // TODO Hvordan skal man koble slettingen sitt tidspunkt til egenregistreringshendelsen?
    fun deleteEgenregistrering(id: EgenregistreringId): Result<Unit, DomainError> {
        return transactional.withTransaction { tx ->
            // Marker egenregistreringen som ikke gjeldende
            val slettetEgenregistrering = egenregistreringRepository.deleteEgenregistrering(id, tx)

            if (slettetEgenregistrering == null) {
                // TODO Hvordan sende noe ut av withTransaction?
                return@withTransaction
            }

            // Finn ut hvilken bruksenhet egenregistreringen tilhører
            val bruksenhetBubbleId = slettetEgenregistrering.bruksenhetRegistrering.bruksenhetBubbleId

            bygningService.getBruksenhetByBubbleId(bruksenhetBubbleId.value, slettetEgenregistrering.registreringstidspunkt)
                .map {
                    // Hent alle egenregistreringer tilknyttet bruksenheten
                    val gjeldendeEgenregistreringerForBruksenhet = egenregistreringRepository.getGjeldendeEgenregistreringerForBruksenhet(
                        slettetEgenregistrering.bruksenhetRegistrering.bruksenhetBubbleId,
                        tx,
                    )

                    it.applyEgenregistreringer(gjeldendeEgenregistreringerForBruksenhet)
                }
                .onSuccess {
                    bygningRepository.saveBruksenhet(
                        bruksenhet = it,
                        // TODO Dette tidspunktet må kanskje deles mellom flere operasjoner?
                        registreringstidspunkt = Instant.now(),
                        tx = tx,
                    )

                    // TODO Hendelse her?
                }
        }.toResultOr {
            // TODO Denne funker ikke, Unit er aldri null
            EgenregistreringNotFound("Egenregistrering med id $id ble ikke funnet")
        }
    }

    private fun createBruksenhetSnapshotOfLatestEgenregistrering(
        bruksenhet: Bruksenhet, egenregistrering: Egenregistrering
    ): Bruksenhet {
        return bruksenhet.applyEgenregistrering(egenregistrering)
    }

    private fun createEgenregistreringHendelsePayloads(egenregistrering: Egenregistrering): BruksenhetOppdatertPayload {
        return BruksenhetOppdatertPayload(
            objectId = egenregistrering.bruksenhetRegistrering.bruksenhetBubbleId.value,
            registreringstidspunkt = egenregistrering.registreringstidspunkt,
        )
    }
}
