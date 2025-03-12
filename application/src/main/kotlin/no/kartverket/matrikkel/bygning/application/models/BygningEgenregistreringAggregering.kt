package no.kartverket.matrikkel.bygning.application.models

import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.BruksenhetEtasjer
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode

fun Bygning.applyEgenregistreringer(egenregistreringer: List<Egenregistrering>): Bygning {
    return egenregistreringer.sortedBy { it.registreringstidspunkt }.fold(this) { bygningAggregate, egenregistrering ->
        bygningAggregate.copy(
            bruksenheter = bygningAggregate.bruksenheter.map {
                it.applyEgenregistrering(egenregistrering)
            },
        )
    }
}

fun Bruksenhet.applyEgenregistreringer(egenregistreringer: List<Egenregistrering>): Bruksenhet {
    return egenregistreringer.sortedBy { it.registreringstidspunkt }.fold(this) { bruksenhetAggregate, egenregistrering ->
        bruksenhetAggregate.applyEgenregistrering(egenregistrering)
    }
}

fun Bruksenhet.applyEgenregistrering(egenregistrering: Egenregistrering): Bruksenhet {
    val bruksenhetRegistrering = egenregistrering.bruksenhetRegistrering

    val metadata = RegisterMetadata(
        registreringstidspunkt = egenregistrering.registreringstidspunkt,
        registrertAv = egenregistrering.eier,
        prosess = egenregistrering.prosess,
        gyldighetsperiode = Gyldighetsperiode(
            gyldighetsdato = null,
            opphoersdato = null,
        ),
    )

    return this.copy(
        byggeaar = this.byggeaar.aggregate(bruksenhetRegistrering.byggeaarRegistrering) {
            Byggeaar(
                data = it.byggeaar,
                metadata = metadata.withKildemateriale(it.kildemateriale),
            )
        },
        totaltBruksareal = this.totaltBruksareal.aggregate(
            registrering = bruksenhetRegistrering.bruksarealRegistrering?.totaltBruksareal,
        ) {
            Bruksareal(
                it,
                metadata.withKildemateriale(bruksenhetRegistrering.bruksarealRegistrering?.kildemateriale),
            )
        },
        vannforsyning = this.vannforsyning.aggregate(bruksenhetRegistrering.vannforsyningRegistrering) {
            Vannforsyning(
                data = it.vannforsyning,
                metadata = metadata.withKildemateriale(it.kildemateriale),
            )
        },
        avlop = this.avlop.aggregate(bruksenhetRegistrering.avlopRegistrering) {
            Avlop(
                data = it.avlop,
                metadata = metadata.withKildemateriale(it.kildemateriale),
            )
        },
        energikilder = this.energikilder.aggregate(bruksenhetRegistrering.energikildeRegistrering) {
            Energikilde(
                data = it.energikilder,
                metadata = metadata.withKildemateriale(it.kildemateriale),
            )
        },

        oppvarming = this.oppvarming.aggregate(bruksenhetRegistrering.oppvarmingRegistrering) {
            val currentOppvarming = oppvarming.egenregistrert ?: emptyList()

            val (newOppvarmingToUpdate, newOppvarmingToAdd) = it.partition { registrering ->
                registrering.kode in currentOppvarming.map { current -> current.data }
            }

            currentOppvarming.map { oppvarming ->
                newOppvarmingToUpdate.find { it.kode == oppvarming.data }?.toOppvarming(metadata) ?: oppvarming
            }.plus(
                newOppvarmingToAdd.map { it.toOppvarming(metadata) },
            )
        },

        etasjer = this.etasjer.aggregate(
            registrering = bruksenhetRegistrering.bruksarealRegistrering?.etasjeRegistreringer,
            shouldRemove = !bruksenhetRegistrering.bruksarealRegistrering.isBothEtasjeAndTotalRegistrert(),
        ) { etasjeBruksarealRegistreringer ->
            BruksenhetEtasjer(
                data = etasjeBruksarealRegistreringer.map {
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

private fun OppvarmingRegistrering.toOppvarming(metadata: RegisterMetadata): Oppvarming {
    return Oppvarming(
        data = this.kode,
        metadata = metadata.withKildemateriale(this.kildemateriale).withGyldighetsperiode(
            Gyldighetsperiode(gyldighetsdato = this.gyldighetsdato, opphoersdato = this.opphoersdato),
        ),
    )
}

private fun BruksarealRegistrering?.isBothEtasjeAndTotalRegistrert(): Boolean =
    this?.totaltBruksareal != null && etasjeRegistreringer?.isNotEmpty() == true

private fun <T : Any, V : Any> Multikilde<T>.aggregate(
    registrering: V?, shouldRemove: Boolean = false, mapper: (V) -> T?
): Multikilde<T> {
    if (shouldRemove) {
        return withEgenregistrert(null)
    }

    if (registrering == null) {
        return this
    }

    return withEgenregistrert(mapper(registrering))
}

private fun RegisterMetadata.withKildemateriale(kildemateriale: KildematerialeKode?): RegisterMetadata {
    return this.copy(kildemateriale = kildemateriale)
}

private fun RegisterMetadata.withGyldighetsperiode(gyldighetsperiode: Gyldighetsperiode): RegisterMetadata {
    return this.copy(gyldighetsperiode = gyldighetsperiode)
}
