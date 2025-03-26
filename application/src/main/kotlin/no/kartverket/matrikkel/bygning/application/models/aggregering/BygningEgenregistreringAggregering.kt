package no.kartverket.matrikkel.bygning.application.models.aggregering

import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BruksenhetEtasje
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.BruksenhetEtasjer
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Felt.EnergikildeOpplysning
import no.kartverket.matrikkel.bygning.application.models.Felt.OppvarmingOpplysning
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.Gyldighetsperiode
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata

fun Bygning.applyEgenregistreringer(egenregistreringer: List<Egenregistrering>): Bygning {
    return egenregistreringer
        .sortedBy { it.registreringstidspunkt }
        .fold(this) { bygningAggregate, egenregistrering ->
            bygningAggregate.copy(
                bruksenheter = bygningAggregate.bruksenheter.map {
                    it.applyEgenregistrering(egenregistrering)
                },
            )
        }
}

fun Bruksenhet.applyEgenregistreringer(egenregistreringer: List<Egenregistrering>): Bruksenhet {
    return egenregistreringer
        .sortedBy { it.registreringstidspunkt }
        .fold(this) { bruksenhetAggregate, egenregistrering ->
            bruksenhetAggregate.applyEgenregistrering(egenregistrering)
        }
}

fun Bruksenhet.applyEgenregistrering(egenregistrering: Egenregistrering): Bruksenhet {
    val bruksenhetRegistrering = egenregistrering.bruksenhetRegistrering

    val metadata = RegisterMetadata(
        registreringstidspunkt = egenregistrering.registreringstidspunkt,
        registrertAv = egenregistrering.eier,
        prosess = egenregistrering.prosess,
        gyldighetsperiode = Gyldighetsperiode.of(),
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
                metadata = metadata
                    .withKildemateriale(it.kildemateriale)
                    .withGyldighetsperiode(
                        Gyldighetsperiode.of(gyldighetsaar = it.gyldighetsaar, opphoersaar = it.opphoersaar),
                    ),
            )
        },
        avlop = this.avlop.aggregate(bruksenhetRegistrering.avlopRegistrering) {
            Avlop(
                data = it.avlop,
                metadata = metadata
                    .withKildemateriale(it.kildemateriale)
                    .withGyldighetsperiode(
                        Gyldighetsperiode.of(gyldighetsaar = it.gyldighetsaar, opphoersaar = it.opphoersaar),
                    ),
            )
        },
        energikilder = this.energikilder.aggregate(bruksenhetRegistrering.energikildeRegistrering) { energikildeRegistrering ->
            when (energikildeRegistrering) {
                is EnergikildeRegistrering.HarIkke -> EnergikildeOpplysning.HarIkke.of(
                    metadata = metadata.withKildemateriale(energikildeRegistrering.kildemateriale),
                )

                is EnergikildeRegistrering.Data -> EnergikildeOpplysning.Data.of(
                    data = oppdaterEnergikilder(
                        energikildeRegistrering = energikildeRegistrering,
                        metadata = metadata,
                        currentEnergikilder = when (this.energikilder.egenregistrert) {
                            is EnergikildeOpplysning.Data -> this.energikilder.egenregistrert.data
                            is EnergikildeOpplysning.HarIkke, null -> emptyList()
                        },
                    )
                )
            }
        },
        oppvarming = this.oppvarming.aggregate(bruksenhetRegistrering.oppvarmingRegistrering) { oppvarmingRegistrering ->
            when (oppvarmingRegistrering) {
                is OppvarmingRegistrering.HarIkke -> OppvarmingOpplysning.HarIkke.of(
                    metadata = metadata.withKildemateriale(oppvarmingRegistrering.kildemateriale),
                )

                is OppvarmingRegistrering.Data -> OppvarmingOpplysning.Data.of(
                    data = oppdaterOppvarming(
                        oppvarmingRegistrering = oppvarmingRegistrering,
                        metadata = metadata,
                        currentOppvarming = when (this.oppvarming.egenregistrert) {
                            is OppvarmingOpplysning.Data -> this.oppvarming.egenregistrert.data
                            is OppvarmingOpplysning.HarIkke, null -> emptyList()
                        }
                    )
                )
            }
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
