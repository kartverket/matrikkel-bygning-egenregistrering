package no.kartverket.matrikkel.bygning.application.models.aggregering

import no.kartverket.matrikkel.bygning.application.models.EnergikildeDataRegistrering
import no.kartverket.matrikkel.bygning.application.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Gyldighetsperiode
import no.kartverket.matrikkel.bygning.application.models.OppvarmingDataRegistrering
import no.kartverket.matrikkel.bygning.application.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata

internal fun oppdaterEnergikilder(
    energikildeRegistrering: EnergikildeRegistrering.Data,
    metadata: RegisterMetadata,
    currentEnergikilder: List<Energikilde>,
): List<Energikilde> {
    val (newEnergikilderToUpdate, newEnergikilderToAdd) =
        energikildeRegistrering.data
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
    oppvarmingRegistrering: OppvarmingRegistrering.Data,
    metadata: RegisterMetadata,
    currentOppvarming: List<Oppvarming> = emptyList(),
): List<Oppvarming> {
    val (newOppvarmingToUpdate, newOppvarmingToAdd) =
        oppvarmingRegistrering.data
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
                    Gyldighetsperiode.of(gyldighetsaar = this.gyldighetsaar, opphoersaar = this.opphoersaar),
                ),
    )

private fun EnergikildeDataRegistrering.toEnergikilde(metadata: RegisterMetadata): Energikilde =
    Energikilde(
        data = this.energikilde,
        metadata =
            metadata
                .withKildemateriale(this.kildemateriale)
                .withGyldighetsperiode(
                    Gyldighetsperiode.of(gyldighetsaar = this.gyldighetsaar, opphoersaar = this.opphoersaar),
                ),
    )
