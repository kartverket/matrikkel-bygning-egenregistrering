package no.kartverket.matrikkel.bygning.application.egenregistrering

import no.kartverket.matrikkel.bygning.application.models.EnergikildeDataRegistrering
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.Gyldighetsperiode
import no.kartverket.matrikkel.bygning.application.models.OppvarmingDataRegistrering
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata

internal fun oppdaterEnergikilder(
    felt: FeltRegistrering.EnergikildeFeltRegistrering.Energikilder,
    metadata: RegisterMetadata,
    currentEnergikilder: List<Energikilde>,
): List<Energikilde> {
    val (newEnergikilderToUpdate, newEnergikilderToAdd) =
        felt.energikilder
            .map {
                it.toEnergikilde(metadata)
            }.partition { energikilde ->
                energikilde.data in currentEnergikilder.map { current -> current.data }
            }

    return currentEnergikilder
        .map { energikilde -> newEnergikilderToUpdate.find { it.data == energikilde.data } ?: energikilde }
        .plus(newEnergikilderToAdd)
}

internal fun oppdaterOppvarming(
    felt: FeltRegistrering.OppvarmingFeltRegistrering.Oppvarming,
    metadata: RegisterMetadata,
    currentOppvarming: List<Oppvarming> = emptyList(),
): List<Oppvarming> {
    val (newOppvarmingToUpdate, newOppvarmingToAdd) =
        felt.oppvarming
            .map {
                it.toOppvarming(metadata)
            }.partition { oppvarming ->
                oppvarming.data in currentOppvarming.map { current -> current.data }
            }

    return currentOppvarming
        .map { oppvarming -> newOppvarmingToUpdate.find { it.data == oppvarming.data } ?: oppvarming }
        .plus(newOppvarmingToAdd)
}

private fun OppvarmingDataRegistrering.toOppvarming(metadata: RegisterMetadata): Oppvarming =
    Oppvarming(
        data = this.oppvarming,
        metadata =
            metadata
                .withKildemateriale(this.kildemateriale)
                .withGyldighetsperiode(
                    Gyldighetsperiode.of(gyldighetsaar = this.gyldighetsaar),
                ),
    )

private fun EnergikildeDataRegistrering.toEnergikilde(metadata: RegisterMetadata): Energikilde =
    Energikilde(
        data = this.energikilde,
        metadata =
            metadata
                .withKildemateriale(this.kildemateriale)
                .withGyldighetsperiode(
                    Gyldighetsperiode.of(gyldighetsaar = this.gyldighetsaar),
                ),
    )
