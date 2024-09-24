package no.kartverket.matrikkel.bygning.models

/**
 * Burde vi kanskje sortere egenregistreringer her, da vi er avhengig av at egenregistreringene er sortert her,
 * men ikke nødvendigvis andre steder?
 */

fun Bygning.withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bygning {
    return egenregistreringer.fold(this) { bygningAggregate, egenregistrering ->
        bygningAggregate.copy(
            byggeaar = bygningAggregate.byggeaar.aggregate {
                egenregistrering.bygningRegistrering.byggeaarRegistrering?.let {
                    Byggeaar(
                        data = it.byggeaar,
                        metadata = RegisterMetadata(
                            registreringstidspunkt = egenregistrering.registreringstidspunkt,
                        ),
                    )
                }
            },
            bruksareal = bygningAggregate.bruksareal.aggregate {
                egenregistrering.bygningRegistrering.bruksarealRegistrering?.let {
                    Bruksareal(
                        data = it.bruksareal,
                        metadata = RegisterMetadata(
                            registreringstidspunkt = egenregistrering.registreringstidspunkt,
                        ),
                    )
                }
            },
            vannforsyning = bygningAggregate.vannforsyning.aggregate {
                egenregistrering.bygningRegistrering.vannforsyningRegistrering?.let {
                    Vannforsyning(
                        data = it.vannforsyning,
                        metadata = RegisterMetadata(
                            registreringstidspunkt = egenregistrering.registreringstidspunkt,
                        ),
                    )
                }
            },
            avlop = bygningAggregate.avlop.aggregate {
                egenregistrering.bygningRegistrering.avlopRegistrering?.let {
                    Avlop(
                        data = it.avlop,
                        metadata = RegisterMetadata(
                            registreringstidspunkt = egenregistrering.registreringstidspunkt,
                        ),
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

    // Jeg er litt usikker på om det er nødvendig å filtrere ut og lage et Pair, så folde på bare bruksenhetregistreringene
    // fremfor å gjøre det på hele bygningregistreringen dersom vi legger til logikk for å kun godkjenne én registrering
    return bruksenhetRegistreringer.fold(this) { bruksenhetAggregate, egenregistrering ->
        bruksenhetAggregate.copy(
            bruksareal = bruksenhetAggregate.bruksareal.aggregate {
                egenregistrering.second.bruksarealRegistrering?.let {
                    Bruksareal(
                        data = it.bruksareal,
                        metadata = RegisterMetadata(
                            registreringstidspunkt = egenregistrering.first,
                        ),
                    )
                }
            },
            energikilder = bruksenhetAggregate.energikilder.aggregate {
                egenregistrering.second.energikildeRegistrering?.let {
                    it.energikilder?.map { registrertKilde ->
                        Energikilde(
                            data = registrertKilde,
                            metadata = RegisterMetadata(
                                registreringstidspunkt = egenregistrering.first,
                            ),
                        )
                    }
                }
            },
            oppvarminger = bruksenhetAggregate.oppvarminger.aggregate {
                egenregistrering.second.oppvarmingRegistrering?.let {
                    it.oppvarminger?.map { registrertOppvarming ->
                        Oppvarming(
                            data = registrertOppvarming,
                            metadata = RegisterMetadata(
                                registreringstidspunkt = egenregistrering.first,
                            ),
                        )
                    }
                }
            },
        )
    }
}

private fun <T : Any> Multikilde<T>.aggregate(mapper: () -> T?): Multikilde<T> =
    takeIf { it.egenregistrert != null } ?: withEgenregistrert(mapper())
