package no.kartverket.matrikkel.bygning.application.egenregistrering

import no.kartverket.matrikkel.bygning.application.models.AvsluttEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.AvsluttFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.AvsluttFeltRegistrering.AvsluttAvlop
import no.kartverket.matrikkel.bygning.application.models.AvsluttFeltRegistrering.AvsluttVannforsyning
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BruksenhetEtasje
import no.kartverket.matrikkel.bygning.application.models.EgenregistreringBase
import no.kartverket.matrikkel.bygning.application.models.Felt
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.AvlopFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.BruksarealFeltRegistrering.TotaltBruksarealFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.BruksarealFeltRegistrering.TotaltOgEtasjeBruksarealFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.ByggeaarFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.EnergikildeFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.OppvarmingFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.VannforsyningFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.Gyldighetsperiode
import no.kartverket.matrikkel.bygning.application.models.KorrigerEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.RegistrerEgenregistrering

class EgenregistreringHandler {
    companion object {
        fun applyEgenregistrering(
            egenregistrering: EgenregistreringBase,
            bruksenhet: Bruksenhet,
        ): Bruksenhet {
            val metadata =
                RegisterMetadata(
                    registreringstidspunkt = egenregistrering.registreringstidspunkt,
                    registrertAv = egenregistrering.eier,
                    prosess = egenregistrering.prosess,
                    gyldighetsperiode = Gyldighetsperiode.of(),
                    egenregistreringId = egenregistrering.id,
                )

            return when (egenregistrering) {
                is RegistrerEgenregistrering ->
                    registrerEllerKorrigerMedFeltRegistrering(
                        bruksenhet,
                        egenregistrering.feltRegistrering,
                        metadata,
                    )

                is KorrigerEgenregistrering ->
                    registrerEllerKorrigerMedFeltRegistrering(
                        bruksenhet,
                        egenregistrering.feltRegistrering,
                        metadata,
                    )

                is AvsluttEgenregistrering -> bruksenhet.avinstallerFelt(egenregistrering.feltRegistrering)
            }
        }

        private fun registrerEllerKorrigerMedFeltRegistrering(
            bruksenhet: Bruksenhet,
            felt: FeltRegistrering,
            metadata: RegisterMetadata,
        ): Bruksenhet =
            when (felt) {
                is ByggeaarFeltRegistrering -> bruksenhet.applyByggeaar(felt, metadata)
                is TotaltBruksarealFeltRegistrering ->
                    bruksenhet.applyTotaltBruksareal(
                        felt,
                        metadata,
                    )
                is TotaltOgEtasjeBruksarealFeltRegistrering ->
                    bruksenhet.applyTotaltOgEtasjeBruksareal(
                        felt,
                        metadata,
                    )
                is VannforsyningFeltRegistrering -> bruksenhet.applyVannforsyning(felt, metadata)
                is AvlopFeltRegistrering -> bruksenhet.applyAvlop(felt, metadata)
                is EnergikildeFeltRegistrering.Energikilder -> bruksenhet.applyEnergikilder(felt, metadata)
                is EnergikildeFeltRegistrering.HarIkke -> TODO()
                is OppvarmingFeltRegistrering.Oppvarming -> bruksenhet.applyOppvarming(felt, metadata)
                is OppvarmingFeltRegistrering.HarIkke -> TODO()
            }

        private fun Bruksenhet.applyByggeaar(
            felt: ByggeaarFeltRegistrering,
            metadata: RegisterMetadata,
        ) = this.copy(
            byggeaar =
                this.byggeaar.withEgenregistrert(
                    Felt.Byggeaar(
                        data = felt.byggeaar,
                        metadata = metadata.withKildemateriale(felt.kildemateriale),
                    ),
                ),
        )

        private fun Bruksenhet.applyTotaltBruksareal(
            felt: TotaltBruksarealFeltRegistrering,
            metadata: RegisterMetadata,
        ): Bruksenhet =
            this.copy(
                totaltBruksareal =
                    this.totaltBruksareal.withEgenregistrert(
                        Felt.Bruksareal(
                            data = felt.totaltBruksareal,
                            metadata = metadata.withKildemateriale(felt.kildemateriale),
                        ),
                    ),
                etasjer = this.etasjer.withEgenregistrert(null),
            )

        private fun Bruksenhet.applyTotaltOgEtasjeBruksareal(
            felt: TotaltOgEtasjeBruksarealFeltRegistrering,
            metadata: RegisterMetadata,
        ): Bruksenhet =
            this.copy(
                totaltBruksareal =
                    this.totaltBruksareal.withEgenregistrert(
                        Felt.Bruksareal(
                            data = felt.totaltBruksareal,
                            metadata = metadata.withKildemateriale(felt.kildemateriale),
                        ),
                    ),
                etasjer =
                    this.etasjer.withEgenregistrert(
                        Felt.BruksenhetEtasjer(
                            data =
                                felt.etasjeRegistreringer.map {
                                    BruksenhetEtasje(
                                        etasjebetegnelse = it.etasjebetegnelse,
                                        bruksareal = it.bruksareal,
                                    )
                                },
                            metadata = metadata.withKildemateriale(felt.kildemateriale),
                        ),
                    ),
            )

        private fun Bruksenhet.applyVannforsyning(
            felt: VannforsyningFeltRegistrering,
            metadata: RegisterMetadata,
        ): Bruksenhet =
            this.copy(
                vannforsyning =
                    this.vannforsyning.withEgenregistrert(
                        Felt.Vannforsyning(
                            data = felt.vannforsyning,
                            metadata =
                                metadata
                                    .withKildemateriale(felt.kildemateriale)
                                    .withGyldighetsperiode(
                                        Gyldighetsperiode.of(gyldighetsaar = felt.gyldighetsaar),
                                    ),
                        ),
                    ),
            )

        private fun Bruksenhet.applyAvlop(
            felt: AvlopFeltRegistrering,
            metadata: RegisterMetadata,
        ) = this.copy(
            avlop =
                this.avlop.withEgenregistrert(
                    Felt.Avlop(
                        data = felt.avlop,
                        metadata =
                            metadata
                                .withKildemateriale(felt.kildemateriale)
                                .withGyldighetsperiode(
                                    Gyldighetsperiode.of(
                                        gyldighetsaar = felt.gyldighetsaar,
                                    ),
                                ),
                    ),
                ),
        )

        private fun Bruksenhet.applyEnergikilder(
            felt: EnergikildeFeltRegistrering.Energikilder,
            metadata: RegisterMetadata,
        ): Bruksenhet =
            this.copy(
                energikilder =
                    this.energikilder.withEgenregistrert(
                        oppdaterEnergikilder(
                            felt = felt,
                            metadata = metadata,
                            currentEnergikilder = energikilder.egenregistrert ?: emptyList(),
                        ),
                    ),
            )

        private fun Bruksenhet.applyOppvarming(
            felt: OppvarmingFeltRegistrering.Oppvarming,
            metadata: RegisterMetadata,
        ): Bruksenhet =
            this.copy(
                oppvarming =
                    oppvarming.withEgenregistrert(
                        oppdaterOppvarming(
                            felt = felt,
                            metadata = metadata,
                            currentOppvarming = oppvarming.egenregistrert ?: emptyList(),
                        ),
                    ),
            )

        private fun Bruksenhet.avinstallerFelt(felt: AvsluttFeltRegistrering): Bruksenhet =
            when (felt) {
                is AvsluttVannforsyning -> {
                    this
                        .copy(
                            vannforsyning =
                                this.vannforsyning.withEgenregistrert(
                                    this.vannforsyning.egenregistrert
                                        ?.withOpphoersaar(felt.opphoersaar)
                                        .takeIf { it?.erOpphoert() == false },
                                ),
                        )
                }
                is AvsluttAvlop -> {
                    this
                        .copy(
                            avlop =
                                this.avlop.withEgenregistrert(
                                    this.avlop.egenregistrert
                                        ?.withOpphoersaar(felt.opphoersaar)
                                        .takeIf { it?.erOpphoert() == false },
                                ),
                        )
                }
            }
    }
}
