package no.kartverket.matrikkel.bygning.application.models

import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.BruksenhetEtasjer
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode

fun Bygning.applyEgenregistreringer(egenregistreringer: List<Egenregistrering>): Bygning =
    egenregistreringer.sortedBy { it.registreringstidspunkt }.fold(this) { bygningAggregate, egenregistrering ->
        bygningAggregate.copy(
            bruksenheter =
                bygningAggregate.bruksenheter.map {
                    it.applyEgenregistrering(egenregistrering)
                },
        )
    }

fun Bruksenhet.applyEgenregistreringer(egenregistreringer: List<Egenregistrering>): Bruksenhet =
    egenregistreringer.sortedBy { it.registreringstidspunkt }.fold(this) { bruksenhetAggregate, egenregistrering ->
        bruksenhetAggregate.applyEgenregistrering(egenregistrering)
    }

fun Bruksenhet.applyEgenregistrering(egenregistrering: Egenregistrering): Bruksenhet {
    val bruksenhetRegistrering = egenregistrering.bruksenhetRegistrering

    val metadata =
        RegisterMetadata(
            registreringstidspunkt = egenregistrering.registreringstidspunkt,
            registrertAv = egenregistrering.eier,
            prosess = egenregistrering.prosess,
            gyldighetsperiode = Gyldighetsperiode.of(),
        )

    return this.copy(
        byggeaar =
            this.byggeaar.aggregate(bruksenhetRegistrering.byggeaarRegistrering) {
                Byggeaar(
                    data = it.byggeaar,
                    metadata = metadata.withKildemateriale(it.kildemateriale),
                )
            },
        totaltBruksareal =
            this.totaltBruksareal.aggregate(
                registrering = bruksenhetRegistrering.bruksarealRegistrering?.totaltBruksareal,
            ) {
                Bruksareal(
                    it,
                    metadata.withKildemateriale(bruksenhetRegistrering.bruksarealRegistrering?.kildemateriale),
                )
            },
        vannforsyning =
            this.vannforsyning.aggregate(bruksenhetRegistrering.vannforsyningRegistrering) { vannforsyningRegistrering ->
                Vannforsyning(
                    data = vannforsyningRegistrering.vannforsyning,
                    metadata =
                        metadata.withKildemateriale(vannforsyningRegistrering.kildemateriale).withGyldighetsperiode(
                            Gyldighetsperiode.of(
                                gyldighetsaar = vannforsyningRegistrering.gyldighetsaar,
                                opphoersaar = vannforsyningRegistrering.opphoersaar,
                            ),
                        ),
                ).takeIf { !it.erOpphoert() }
            },
        avlop =
            this.avlop.aggregate(bruksenhetRegistrering.avlopRegistrering) { avlopRegistrering ->
                Avlop(
                    data = avlopRegistrering.avlop,
                    metadata =
                        metadata.withKildemateriale(avlopRegistrering.kildemateriale).withGyldighetsperiode(
                            Gyldighetsperiode.of(
                                gyldighetsaar = avlopRegistrering.gyldighetsaar,
                                opphoersaar = avlopRegistrering.opphoersaar,
                            ),
                        ),
                ).takeIf { !it.erOpphoert() }
            },
        energikilder =
            this.energikilder.aggregate(bruksenhetRegistrering.energikildeRegistrering) { energikildeRegistrering ->
                val currentEnergikilder = energikilder.egenregistrert.orEmpty()

                val (newEnergikilderToUpdate, newEnergikilderToAdd) =
                    energikildeRegistrering
                        .map {
                            it.toEnergikilde(
                                metadata,
                            )
                        }.partition { energikilde ->
                            energikilde.data in currentEnergikilder.map { current -> current.data }
                        }

                currentEnergikilder
                    .map { energikilde ->
                        newEnergikilderToUpdate.find { it.data == energikilde.data } ?: energikilde
                    }.plus(newEnergikilderToAdd)
                    .filter { !it.erOpphoert() }
            },
        oppvarming =
            this.oppvarming.aggregate(bruksenhetRegistrering.oppvarmingRegistrering) { oppvarmingRegistrering ->
                val currentOppvarming = oppvarming.egenregistrert.orEmpty()

                val (newOppvarmingToUpdate, newOppvarmingToAdd) =
                    oppvarmingRegistrering
                        .map { it.toOppvarming(metadata) }
                        .partition { oppvarming ->
                            oppvarming.data in currentOppvarming.map { current -> current.data }
                        }

                currentOppvarming
                    .map { oppvarming ->
                        newOppvarmingToUpdate.find { it.data == oppvarming.data } ?: oppvarming
                    }.plus(newOppvarmingToAdd)
                    .filter { !it.erOpphoert() }
            },
        etasjer =
            this.etasjer.aggregate(
                registrering = bruksenhetRegistrering.bruksarealRegistrering?.etasjeRegistreringer,
                shouldRemove = !bruksenhetRegistrering.bruksarealRegistrering.isBothEtasjeAndTotalRegistrert(),
            ) { etasjeBruksarealRegistreringer ->
                BruksenhetEtasjer(
                    data =
                        etasjeBruksarealRegistreringer.map {
                            BruksenhetEtasje(
                                etasjebetegnelse = it.etasjebetegnelse,
                                bruksareal = it.bruksareal,
                            )
                        },
                    metadata = metadata.withKildemateriale(bruksenhetRegistrering.bruksarealRegistrering?.kildemateriale),
                )
            },
    )
}

private fun OppvarmingRegistrering.toOppvarming(metadata: RegisterMetadata): Oppvarming =
    Oppvarming(
        data = this.oppvarming,
        metadata =
            metadata.withKildemateriale(this.kildemateriale).withGyldighetsperiode(
                Gyldighetsperiode.of(gyldighetsaar = this.gyldighetsaar, opphoersaar = this.opphoersaar),
            ),
    )

private fun EnergikildeRegistrering.toEnergikilde(metadata: RegisterMetadata): Energikilde =
    Energikilde(
        data = this.energikilde,
        metadata =
            metadata.withKildemateriale(this.kildemateriale).withGyldighetsperiode(
                Gyldighetsperiode.of(gyldighetsaar = this.gyldighetsaar, opphoersaar = this.opphoersaar),
            ),
    )

private fun BruksarealRegistrering?.isBothEtasjeAndTotalRegistrert(): Boolean =
    this?.totaltBruksareal != null && etasjeRegistreringer?.isNotEmpty() == true

private fun <T : Any, V : Any> Multikilde<T>.aggregate(
    registrering: V?,
    shouldRemove: Boolean = false,
    mapper: (V) -> T?,
): Multikilde<T> {
    if (shouldRemove) {
        return withEgenregistrert(null)
    }

    if (registrering == null) {
        return this
    }

    return withEgenregistrert(mapper(registrering))
}

private fun RegisterMetadata.withKildemateriale(kildemateriale: KildematerialeKode?): RegisterMetadata =
    this.copy(kildemateriale = kildemateriale)

private fun RegisterMetadata.withGyldighetsperiode(gyldighetsperiode: Gyldighetsperiode): RegisterMetadata =
    this.copy(gyldighetsperiode = gyldighetsperiode)
