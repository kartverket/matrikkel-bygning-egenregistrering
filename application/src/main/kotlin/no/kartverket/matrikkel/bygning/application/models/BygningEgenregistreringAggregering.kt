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
        byggeaar = this.byggeaar.aggregate(bruksenhetRegistrering.byggeaarRegistrering) {
            Byggeaar(
                data = it.byggeaar,
                metadata = metadata,
            )
        },
        totaltBruksareal = this.totaltBruksareal.aggregate(
            registrering = bruksenhetRegistrering.bruksarealRegistrering?.totaltBruksareal,
            shouldMapRegistrering = !this.isEgenregistrertBruksarealRegistreringPresent(),
        ) {
            Bruksareal(
                it,
                metadata = metadata,
            )
        },
        vannforsyning = this.vannforsyning.aggregate(bruksenhetRegistrering.vannforsyningRegistrering) {
            Vannforsyning(
                data = it.vannforsyning,
                metadata = metadata,
            )
        },
        avlop = this.avlop.aggregate(bruksenhetRegistrering.avlopRegistrering) {
            Avlop(
                data = it.avlop,
                metadata = metadata,
            )
        },
        energikilder = this.energikilder.aggregate(bruksenhetRegistrering.energikildeRegistrering) {
            it.energikilder?.map { registrertKilde ->
                Energikilde(
                    data = registrertKilde,
                    metadata = metadata,
                )
            }
        },
        oppvarminger = this.oppvarminger.aggregate(bruksenhetRegistrering.oppvarmingRegistrering) {
            it.oppvarminger?.map { registrertOppvarming ->
                Oppvarming(
                    data = registrertOppvarming,
                    metadata = metadata,
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
                        metadata = metadata,
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


private fun <T : Any, V : Any> Multikilde<T>.aggregate(registrering: V?, shouldMapRegistrering: Boolean = true, mapper: (V) -> T?): Multikilde<T> {
    if (this.egenregistrert != null || registrering == null) {
        return this
    }

    return withEgenregistrert(if (shouldMapRegistrering) mapper(registrering) else null)
}
