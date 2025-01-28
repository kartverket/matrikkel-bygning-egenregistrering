package no.kartverket.matrikkel.bygning.application.models

import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode

/**
 * Burde vi kanskje sortere egenregistreringer her, da vi er avhengig av at egenregistreringene er sortert her,
 * men ikke nødvendigvis andre steder?
 */

fun Bygning.withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bygning {
    return egenregistreringer.fold(this) { bygningAggregate, egenregistrering ->
        bygningAggregate.copy(
            bruksenheter = bygningAggregate.bruksenheter.map {
                it.applyEgenregistrering(egenregistrering)
            },
        )
    }
}

private fun Bruksenhet.applyEgenregistrering(egenregistrering: Egenregistrering): Bruksenhet {
    val bruksenhetRegistrering =
        egenregistrering.bygningRegistrering.bruksenhetRegistreringer.firstOrNull { it.bruksenhetBubbleId == this.bruksenhetBubbleId }
    if (bruksenhetRegistrering == null) {
        return this
    }

    val metadata = RegisterMetadata(
        registreringstidspunkt = egenregistrering.registreringstidspunkt,
        registrertAv = egenregistrering.eier,
        prosess = egenregistrering.prosess,
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
            it.energikilder?.map { registrertKilde ->
                Energikilde(
                    data = registrertKilde,
                    metadata = metadata.withKildemateriale(it.kildemateriale),
                )
            }
        },
        oppvarminger = this.oppvarminger.aggregate(bruksenhetRegistrering.oppvarmingRegistrering) {
            it.oppvarminger?.map { registrertOppvarming ->
                Oppvarming(
                    data = registrertOppvarming,
                    metadata = metadata.withKildemateriale(it.kildemateriale),
                )
            }
        },

        etasjer = this.etasjer.aggregate(
            registrering = bruksenhetRegistrering.bruksarealRegistrering?.etasjeRegistreringer,
            shouldMapRegistrering = !this.isEgenregistrertBruksarealRegistreringPresent(),
        ) {
            it.map {
                BruksenhetEtasje(
                    etasjebetegnelse = it.etasjebetegnelse,
                    bruksareal = Bruksareal(
                        data = it.bruksareal,
                        metadata = metadata.withKildemateriale(bruksenhetRegistrering.bruksarealRegistrering?.kildemateriale),
                    ),
                )
            }
        },
    )
}

private fun Bruksenhet.isEgenregistrertBruksarealRegistreringPresent(): Boolean =
    this.etasjer.egenregistrert != null || this.totaltBruksareal.egenregistrert != null

fun Bruksenhet.withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bruksenhet {
    return egenregistreringer.fold(this) { bruksenhetAggregate, egenregistrering ->
        bruksenhetAggregate.applyEgenregistrering(egenregistrering)
    }
}

fun Bruksenhet.withEgenregistrertData(egenregistrering: Egenregistrering): Bruksenhet {
    return this.withEgenregistrertData(listOf(egenregistrering))
}

private fun <T : Any, V : Any> Multikilde<T>.aggregate(
    registrering: V?, shouldMapRegistrering: Boolean = true, mapper: (V) -> T?
): Multikilde<T> {
    if (registrering == null) {
        return this
    }

    return withEgenregistrert(if (shouldMapRegistrering) mapper(registrering) else null)
}

private fun RegisterMetadata.withKildemateriale(kildemateriale: KildematerialeKode?): RegisterMetadata {
    return this.copy(kildemateriale = kildemateriale)
}
