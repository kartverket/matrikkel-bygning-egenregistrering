package no.kartverket.matrikkel.bygning.application.models

import no.kartverket.matrikkel.bygning.application.models.Etasje.*

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
        egenregistrering.bygningRegistrering.bruksenhetRegistreringer.firstOrNull { it.bruksenhetId == this.bruksenhetId }
    if (bruksenhetRegistrering == null) {
        return this
    }

    val metadata = RegisterMetadata(
        registreringstidspunkt = egenregistrering.registreringstidspunkt,
        registrertAv = egenregistrering.eier,
    )

    val etasjerInRegistreringNotOnBruksenhet =
        bruksenhetRegistrering.bruksarealRegistrering?.etasjeRegistreringer?.filter { etasjeRegistrering ->
            this.etasjer.find { etasje -> etasje.etasjenummer == etasjeRegistrering.etasjenummer } == null
        }?.map {
            BruksenhetEtasje(
                etasjenummer = it.etasjenummer,
                bruksareal = Multikilde(),
            )
        }

    return this.copy(
        byggeaar = this.byggeaar.aggregate {
            bruksenhetRegistrering.byggeaarRegistrering?.let {
                Byggeaar(
                    data = it.byggeaar,
                    metadata = metadata,
                )
            }
        },
        totalBruksareal = this.totalBruksareal.aggregate {
            bruksenhetRegistrering.bruksarealRegistrering?.totalBruksareal?.let {
                Bruksareal(
                    data = it,
                    metadata = metadata,
                )
            }
        },
        vannforsyning = this.vannforsyning.aggregate {
            bruksenhetRegistrering.vannforsyningRegistrering?.let {
                Vannforsyning(
                    data = it.vannforsyning,
                    metadata = metadata,
                )
            }
        },
        avlop = this.avlop.aggregate {
            bruksenhetRegistrering.avlopRegistrering?.let {
                Avlop(
                    data = it.avlop,
                    metadata = metadata,
                )
            }
        },
        energikilder = this.energikilder.aggregate {
            bruksenhetRegistrering.energikildeRegistrering?.let {
                it.energikilder?.map { registrertKilde ->
                    Energikilde(
                        data = registrertKilde,
                        metadata = metadata,
                    )
                }
            }
        },
        oppvarminger = this.oppvarminger.aggregate {
            bruksenhetRegistrering.oppvarmingRegistrering?.let {
                it.oppvarminger?.map { registrertOppvarming ->
                    Oppvarming(
                        data = registrertOppvarming,
                        metadata = metadata,
                    )
                }
            }
        },
        etasjer = this.etasjer.plus(etasjerInRegistreringNotOnBruksenhet ?: emptyList()).map {
            it.applyBruksenhetRegistrering(bruksenhetRegistrering, metadata)
        },
    )
}

fun BruksenhetEtasje.applyBruksenhetRegistrering(
    bruksenhetRegistrering: BruksenhetRegistrering, metadata: RegisterMetadata
): BruksenhetEtasje {
    val relevantEtasjeRegistrering =
        bruksenhetRegistrering.bruksarealRegistrering?.etasjeRegistreringer?.find { it.etasjenummer == this.etasjenummer }

    if (relevantEtasjeRegistrering == null || this.bruksareal.egenregistrert != null) {
        return this
    }

    return this.copy(
        bruksareal = this.bruksareal.aggregate {
            Bruksareal(
                data = relevantEtasjeRegistrering.bruksareal,
                metadata = metadata,
            )
        },
    )
}


fun Bruksenhet.withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bruksenhet {
    return egenregistreringer.fold(this) { bruksenhetAggregate, egenregistrering ->
        bruksenhetAggregate.applyEgenregistrering(egenregistrering)
    }
}

private fun <T : Any> Multikilde<T>.aggregate(mapper: () -> T?): Multikilde<T> =
    takeIf { it.egenregistrert != null } ?: withEgenregistrert(mapper())
