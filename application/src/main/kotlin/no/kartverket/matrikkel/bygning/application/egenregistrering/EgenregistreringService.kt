package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import no.kartverket.matrikkel.bygning.application.bygning.BygningRepository
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.hendelser.HendelsePayload.BruksenhetOppdatertPayload
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseRepository
import no.kartverket.matrikkel.bygning.application.models.AvinstalleringKommando
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering2
import no.kartverket.matrikkel.bygning.application.models.Felt
import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.EnergikildeOpplysning
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.AvlopFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.ByggeaarFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.EnergikildeFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.VannforsyningFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.Gyldighetsperiode
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer
import no.kartverket.matrikkel.bygning.application.models.aggregering.applyEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.aggregering.toEnergikilde
import no.kartverket.matrikkel.bygning.application.models.aggregering.withGyldighetsperiode
import no.kartverket.matrikkel.bygning.application.models.aggregering.withKildemateriale
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.transaction.Transactional
import java.time.Instant
import java.time.Year

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

    private fun createBruksenhetSnapshotOfLatestEgenregistrering(
        bruksenhet: Bruksenhet, egenregistrering: Egenregistrering
    ): Bruksenhet {
        return bruksenhet.applyEgenregistrering(egenregistrering)
    }

    fun addEgenregistrering(egenregistrering: Egenregistrering2): Result<Unit, DomainError> {
        return bygningService.getBruksenhetByBubbleId(
            bruksenhetBubbleId = egenregistrering.bruksenhetId.value,
        ).map { bruksenhet ->
            val feltRegistrering = egenregistrering.feltRegistrering

            // Hvis ugyldig kommando for nåværende aggregat-tilstand -> returner feil
            if (!bruksenhet.kanHaandtereRegistrering(feltRegistrering)) {
                throw IllegalArgumentException("Kan ikke håndtere kommando")
            }

            // Alt bra, lagre og oppdater aggregat
            transactional.withTransaction { tx ->
                egenregistreringRepository.saveEgenregistrering(egenregistrering, tx)
                bygningRepository.saveBruksenhet(
                    bruksenhet = createBruksenhetSnapshotOfLatestEgenregistrering(bruksenhet, egenregistrering),
                    registreringstidspunkt = egenregistrering.registreringstidspunkt,
                    tx = tx,
                )

                // generer hendelse
            }
        }
    }

    fun avinstaller(avinstalleringKommando: AvinstalleringKommando) {
        bygningService.getBruksenhetByBubbleId(
            bruksenhetBubbleId = avinstalleringKommando.bruksenhetId.value,
        ).map { bruksenhet ->
            // Valider at kommandoen er gyldig


            val oppdatertBruksenhet = when (avinstalleringKommando) {
                is AvinstalleringKommando.AvinstallerVannforsyning -> {
                    bruksenhet.copy(
                        vannforsyning = bruksenhet.vannforsyning.withEgenregistrert(
                            Felt.Vannforsyning(
                                data = bruksenhet.vannforsyning.egenregistrert!!.data!!,
                                metadata = RegisterMetadata(
                                    registrertAv = RegistreringAktoer.Foedselsnummer("66860475309"),
                                    registreringstidspunkt = Instant.now(),
                                    prosess = ProsessKode.Egenregistrering,
                                    kildemateriale = bruksenhet.vannforsyning.egenregistrert!!.metadata!!.kildemateriale,
                                    gyldighetsperiode = Gyldighetsperiode.of(
                                        gyldighetsaar = bruksenhet.vannforsyning.egenregistrert!!.metadata!!.gyldighetsperiode.gyldighetsaar,
                                        opphoersaar = Year.of(
                                            avinstalleringKommando.opphoersaar,
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    )
                }
            }
            transactional.withTransaction { tx ->
                //egenregistreringRepository.saveEgenregistrering(egenregistrering, tx)
                bygningRepository.saveBruksenhet(
                    bruksenhet = oppdatertBruksenhet,
                    registreringstidspunkt = Instant.now(),
                    tx = tx,
                )

                // generer hendelse
            }
        }
    }


    private fun createBruksenhetSnapshotOfLatestEgenregistrering(
        eksisterendeBruksenhet: Bruksenhet,
        egenregistrering: Egenregistrering2
    ): Bruksenhet {
        val metadata = RegisterMetadata(
            registreringstidspunkt = egenregistrering.registreringstidspunkt,
            registrertAv = egenregistrering.eier,
            prosess = egenregistrering.prosess,
            gyldighetsperiode = Gyldighetsperiode.of(),
        )
        return applyFeltRegistrering(eksisterendeBruksenhet, egenregistrering.feltRegistrering, metadata)
    }

    private fun applyFeltRegistrering(bruksenhet: Bruksenhet, felt: FeltRegistrering, metadata: RegisterMetadata): Bruksenhet =
        when (felt) {
            is VannforsyningFeltRegistrering -> bruksenhet.applyVannforsyning(felt, metadata)
            is ByggeaarFeltRegistrering -> bruksenhet.applyByggeaar(felt, metadata)
            is AvlopFeltRegistrering -> bruksenhet.applyAvlop(felt, metadata)
            is EnergikildeFeltRegistrering -> bruksenhet.applyEnergikilde(felt, metadata)
            is FeltRegistrering.BruksarealFeltRegistrering.TotaltBruksArealFeltRegistrering -> TODO()
            is FeltRegistrering.BruksarealFeltRegistrering.TotaltOgEtasjeBruksArealFeltRegistrering -> TODO()
        }

    private fun Bruksenhet.applyByggeaar(
        felt: ByggeaarFeltRegistrering,
        metadata: RegisterMetadata
    ) = this.copy(
        byggeaar = this.byggeaar.withEgenregistrert(
            Byggeaar(
                data = felt.byggeaar,
                metadata = metadata.withKildemateriale(felt.kildemateriale),
            ),
        ),
    )

    private fun Bruksenhet.applyVannforsyning(
        felt: VannforsyningFeltRegistrering,
        metadata: RegisterMetadata
    ): Bruksenhet {
        return this.copy(
            vannforsyning = this.vannforsyning.withEgenregistrert(
                Felt.Vannforsyning(
                    data = felt.vannforsyning,
                    metadata = metadata
                        .withKildemateriale(felt.kildemateriale)
                        .withGyldighetsperiode(
                            Gyldighetsperiode.of(gyldighetsaar = felt.gyldighetsaar),
                        ),
                ),
            ),
        )
    }

    private fun Bruksenhet.applyAvlop(
        felt: AvlopFeltRegistrering,
        metadata: RegisterMetadata
    ) = this.copy(
        avlop = this.avlop.withEgenregistrert(
            Avlop(
                data = felt.avlop,
                metadata = metadata
                    .withKildemateriale(felt.kildemateriale)
                    .withGyldighetsperiode(
                        Gyldighetsperiode.of(gyldighetsaar = felt.gyldighetsaar, opphoersaar = felt.opphoersaar),
                    ),
            ),
        ),
    )

    private fun Bruksenhet.applyEnergikilde(
        felt: EnergikildeFeltRegistrering,
        metadata: RegisterMetadata
    ) = this.copy(
        energikilder = this.energikilder.withEgenregistrert(
            when (felt) {
                is EnergikildeFeltRegistrering.HarIkke -> EnergikildeOpplysning.HarIkke.of(
                    metadata = metadata.withKildemateriale(felt.kildemateriale),
                )

                is EnergikildeFeltRegistrering.Data -> EnergikildeOpplysning.Data.of(
                    data = oppdaterEnergikilder(
                        energikildeRegistrering = felt,
                        metadata = metadata,
                        currentEnergikilder = when (this.energikilder.egenregistrert) {
                            is EnergikildeOpplysning.Data -> this.energikilder.egenregistrert.data
                            is EnergikildeOpplysning.HarIkke, null -> emptyList()
                        },
                    ),
                )
            },
        ),
    )

    private fun oppdaterEnergikilder(
        energikildeRegistrering: EnergikildeFeltRegistrering.Data,
        metadata: RegisterMetadata,
        currentEnergikilder: List<Energikilde> = emptyList()
    ): List<Energikilde> {
        val (newEnergikilderToUpdate, newEnergikilderToAdd) = energikildeRegistrering.data
            .map {
                it.toEnergikilde(metadata)
            }
            .partition { energikilde ->
                energikilde.data in currentEnergikilder.map { current -> current.data }
            }

        return currentEnergikilder
            .map { energikilde -> newEnergikilderToUpdate.find { it.data == energikilde.data } ?: energikilde }
            .plus(newEnergikilderToAdd)
    }

    private fun createEgenregistreringHendelsePayloads(egenregistrering: Egenregistrering): BruksenhetOppdatertPayload {
        return BruksenhetOppdatertPayload(
            objectId = egenregistrering.bruksenhetRegistrering.bruksenhetBubbleId.value,
            registreringstidspunkt = egenregistrering.registreringstidspunkt,
        )
    }
}

private fun Bruksenhet.kanHaandtereRegistrering(feltRegistrering: FeltRegistrering) = when (feltRegistrering) {
    is VannforsyningFeltRegistrering -> this.vannforsyning.egenregistrert == null
    is AvlopFeltRegistrering -> TODO()
    is ByggeaarFeltRegistrering -> TODO()
    is EnergikildeFeltRegistrering.Data -> TODO()
    is EnergikildeFeltRegistrering.HarIkke -> TODO()
    is FeltRegistrering.BruksarealFeltRegistrering.TotaltBruksArealFeltRegistrering -> TODO()
    is FeltRegistrering.BruksarealFeltRegistrering.TotaltOgEtasjeBruksArealFeltRegistrering -> TODO()
}
