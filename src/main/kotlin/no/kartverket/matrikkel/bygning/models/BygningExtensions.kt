package no.kartverket.matrikkel.bygning.models

/**
 * Burde vi kanskje sortere egenregistreringer her, da vi er avhengig av at egenregistreringene er sortert her,
 * men ikke nødvendigvis andre steder?
 */

fun Bygning.withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bygning {
    return egenregistreringer.fold(this) { bygningAggregate, egenregistrering ->
        val metadata = RegisterMetadata(
            registreringstidspunkt = egenregistrering.registreringstidspunkt,
            registrertAv = egenregistrering.eier.getValue(),
        )

        bygningAggregate.copy(
            byggeaar = bygningAggregate.byggeaar.aggregate {
                egenregistrering.bygningRegistrering.byggeaarRegistrering?.let {
                    Byggeaar(
                        data = it.byggeaar,
                        metadata = metadata,
                    )
                }
            },
            bruksareal = bygningAggregate.bruksareal.aggregate {
                egenregistrering.bygningRegistrering.bruksarealRegistrering?.let {
                    Bruksareal(
                        data = it.bruksareal,
                        metadata = metadata,
                    )
                }
            },
            vannforsyning = bygningAggregate.vannforsyning.aggregate {
                egenregistrering.bygningRegistrering.vannforsyningRegistrering?.let {
                    Vannforsyning(
                        data = it.vannforsyning,
                        metadata = metadata,
                    )
                }
            },
            avlop = bygningAggregate.avlop.aggregate {
                egenregistrering.bygningRegistrering.avlopRegistrering?.let {
                    Avlop(
                        data = it.avlop,
                        metadata = metadata,
                    )
                }
            },
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
        registrertAv = egenregistrering.eier.getValue(),
    )

    return this.copy(
        bruksareal = this.bruksareal.aggregate {
            bruksenhetRegistrering.bruksarealRegistrering?.let {
                Bruksareal(
                    data = it.bruksareal,
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
    )
}


fun Bruksenhet.withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bruksenhet {
    return egenregistreringer.fold(this) { bruksenhetAggregate, egenregistrering ->
        bruksenhetAggregate.applyEgenregistrering(egenregistrering)
    }
}

private fun <T : Any> Multikilde<T>.aggregate(mapper: () -> T?): Multikilde<T> =
    takeIf { it.egenregistrert != null } ?: withEgenregistrert(mapper())
