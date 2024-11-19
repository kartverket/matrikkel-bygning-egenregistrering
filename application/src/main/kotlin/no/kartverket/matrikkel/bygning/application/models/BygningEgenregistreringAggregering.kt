package no.kartverket.matrikkel.bygning.application.models

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

    return this.copy(
        byggeaar = this.byggeaar.aggregate {
            bruksenhetRegistrering.byggeaarRegistrering?.let {
                Byggeaar(
                    data = it.byggeaar,
                    metadata = metadata,
                )
            }
        },
        totalBruksareal = this.aggregateTotalBruksareal(bruksenhetRegistrering.bruksarealRegistrering, metadata),
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
        etasjer = aggregateEtasjer(bruksenhetRegistrering.bruksarealRegistrering, metadata),
    )
}

private fun Bruksenhet.aggregateEtasjer(
    bruksarealRegistrering: BruksarealRegistrering?, metadata: RegisterMetadata
): Multikilde<List<BruksenhetEtasje>> {
    if (this.etasjer.egenregistrert != null || this.totalBruksareal.egenregistrert != null || bruksarealRegistrering?.etasjeRegistreringer == null) {
        return this.etasjer
    }

    return this.etasjer.copy(
        egenregistrert = bruksarealRegistrering.etasjeRegistreringer.map {
            BruksenhetEtasje(
                etasjeBetegnelse = it.etasjeBetegnelse,
                bruksareal = Bruksareal(
                    data = it.bruksareal,
                    metadata = metadata,
                ),
            )
        },
    )
}

private fun Bruksenhet.aggregateTotalBruksareal(
    bruksarealRegistrering: BruksarealRegistrering?, metadata: RegisterMetadata
): Multikilde<Bruksareal> {
    if (this.etasjer.egenregistrert != null || this.totalBruksareal.egenregistrert != null || bruksarealRegistrering?.totalBruksareal == null) {
        return this.totalBruksareal
    }

    return this.totalBruksareal.copy(
        egenregistrert = Bruksareal(
            data = bruksarealRegistrering.totalBruksareal,
            metadata = metadata,
        ),
    )
}

fun Bruksenhet.withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bruksenhet {
    return egenregistreringer.fold(this) { bruksenhetAggregate, egenregistrering ->
        bruksenhetAggregate.applyEgenregistrering(egenregistrering)
    }
}

private fun <T : Any> Multikilde<T>.aggregate(mapper: () -> T?): Multikilde<T> =
    takeIf { it.egenregistrert != null } ?: withEgenregistrert(mapper())
