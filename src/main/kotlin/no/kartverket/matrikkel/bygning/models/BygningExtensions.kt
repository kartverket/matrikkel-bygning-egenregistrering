package no.kartverket.matrikkel.bygning.models

import no.kartverket.matrikkel.bygning.models.valuetype.Foedselsnummer

/**
 * Burde vi kanskje sortere egenregistreringer her, da vi er avhengig av at egenregistreringene er sortert her,
 * men ikke nødvendigvis andre steder?
 */

fun Bygning.withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bygning {
    return egenregistreringer.fold(this) { bygningAggregate, egenregistrering ->
        val metadata = RegisterMetadata.Egenregistrert(
            registreringstidspunkt = egenregistrering.registreringstidspunkt,
            eier = egenregistrering.eier,
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
        )
    }
}


fun Bruksenhet.withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bruksenhet {
    // Her så antar jeg at man bare har registrert bruksenheten én gang per registrering, som gir mening
    // Likevel har vi ikke noen logisk sjekk på dette ved registrering, så det bør vi nok ha
    val bruksenhetRegistreringer = egenregistreringer.mapNotNull { egenregistrering ->
        val bruksenhetRegistrering =
            egenregistrering.bygningRegistrering.bruksenhetRegistreringer.firstOrNull { it.bruksenhetId == this.bruksenhetId }

        if (bruksenhetRegistrering != null) {
            egenregistrering.registreringstidspunkt to bruksenhetRegistrering
        } else {
            null
        }
    }
    // TODO Trenger både registreringstidspunkt og eier her, ikke bare tidspunkt. Pair holder ikke helt lenger

    // Jeg er litt usikker på om det er nødvendig å filtrere ut og lage et Pair, så folde på bare bruksenhetregistreringene
    // fremfor å gjøre det på hele bygningregistreringen dersom vi legger til logikk for å kun godkjenne én registrering
    return bruksenhetRegistreringer.fold(this) { bruksenhetAggregate, egenregistrering ->
        val metadata = RegisterMetadata.Egenregistrert(
            registreringstidspunkt = egenregistrering.first,
            eier = Foedselsnummer("TODO"),
        )
        bruksenhetAggregate.copy(
            bruksareal = bruksenhetAggregate.bruksareal.aggregate {
                egenregistrering.second.bruksarealRegistrering?.let {
                    Bruksareal(
                        data = it.bruksareal,
                        metadata = metadata,
                    )
                }
            },
            energikilder = bruksenhetAggregate.energikilder.aggregate {
                egenregistrering.second.energikildeRegistrering?.let {
                    it.energikilder?.map { registrertKilde ->
                        Energikilde(
                            data = registrertKilde,
                            metadata = metadata,
                        )
                    }
                }
            },
            oppvarminger = bruksenhetAggregate.oppvarminger.aggregate {
                egenregistrering.second.oppvarmingRegistrering?.let {
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
}

private fun <T : Any> Multikilde<T>.aggregate(mapper: () -> T?): Multikilde<T> =
    takeIf { it.egenregistrert != null } ?: withEgenregistrert(mapper())
