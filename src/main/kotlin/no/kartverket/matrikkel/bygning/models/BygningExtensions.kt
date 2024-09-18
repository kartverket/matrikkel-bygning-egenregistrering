package no.kartverket.matrikkel.bygning.models

/**
 * Burde vi kanskje sortere egenregistreringer her, da vi er avhengig av at egenregistreringene er sortert her,
 * men ikke nødvendigvis andre steder?
 */

fun Bygning.withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bygning {
    return egenregistreringer.fold(this) { bygningAggregate, egenregistrering ->
        Bygning(
            bygningId = bygningAggregate.bygningId,
            bygningsnummer = bygningAggregate.bygningsnummer,
            bruksenheter = bygningAggregate.bruksenheter,
            byggeaar = bygningAggregate.byggeaar ?: egenregistrering.bygningRegistrering.byggeaarRegistrering?.let {
                Byggeaar(
                    data = it.byggeaar,
                    metadata = RegisterMetadata(
                        registreringstidspunkt = egenregistrering.registreringstidspunkt,
                    ),
                )
            },
            bruksareal = bygningAggregate.bruksareal ?: egenregistrering.bygningRegistrering.bruksarealRegistrering?.let {
                Bruksareal(
                    data = it.bruksareal,
                    metadata = RegisterMetadata(
                        registreringstidspunkt = egenregistrering.registreringstidspunkt,
                    ),
                )
            },
            vannforsyning = bygningAggregate.vannforsyning ?: egenregistrering.bygningRegistrering.vannforsyningRegistrering?.let {
                Vannforsyning(
                    data = it.vannforsyning,
                    metadata = RegisterMetadata(
                        registreringstidspunkt = egenregistrering.registreringstidspunkt,
                    ),
                )
            },
            avlop = bygningAggregate.avlop ?: egenregistrering.bygningRegistrering.avlopRegistrering?.let {
                Avlop(
                    data = it.avlop,
                    metadata = RegisterMetadata(
                        registreringstidspunkt = egenregistrering.registreringstidspunkt,
                    ),
                )
            },
        )
    }
}


fun Bruksenhet.withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bruksenhet {
    // Her så antar jeg at man bare har registrert bruksenheten én gang per registrering, som gir mening
    // Likevel har vi ikke noen logisk sjekk på dette ved registrering, så det bør vi nok ha
    val bruksenhetRegistreringer = egenregistreringer.mapNotNull { egenregistrering ->
        val bruksenhetRegistrering =
            egenregistrering.bygningRegistrering.bruksenhetRegistreringer.filter { it.bruksenhetId == this.bruksenhetId }.firstOrNull()

        if (bruksenhetRegistrering != null) {
            egenregistrering.registreringstidspunkt to bruksenhetRegistrering
        } else {
            null
        }
    }

    // Jeg er litt usikker på om det er nødvendig å filtrere ut og lage et Pair, så folde på bare bruksenhetregistreringene
    // fremfor å gjøre det på hele bygningregistreringen dersom vi legger til logikk for å kun godkjenne én registrering
    return bruksenhetRegistreringer.fold(this) { bruksenhetAggregate, egenregistrering ->
        Bruksenhet(
            bruksenhetId = bruksenhetAggregate.bruksenhetId,
            bygningId = bruksenhetAggregate.bygningId,
            bruksareal = bruksenhetAggregate.bruksareal ?: egenregistrering.second.bruksarealRegistrering?.let {
                Bruksareal(
                    data = it.bruksareal,
                    metadata = RegisterMetadata(
                        registreringstidspunkt = egenregistrering.first,
                    ),
                )
            },
            energikilder = bruksenhetAggregate.energikilder.takeIf { it.isNotEmpty() }
                ?: egenregistrering.second.energikildeRegistrering?.let {
                    it.energikilder?.map { registrertKilde ->
                        Energikilde(
                            data = registrertKilde,
                            metadata = RegisterMetadata(
                                registreringstidspunkt = egenregistrering.first,
                            ),
                        )
                    }
                } ?: emptyList(),
            oppvarminger = bruksenhetAggregate.oppvarminger.takeIf { it.isNotEmpty() }
                ?: egenregistrering.second.oppvarmingRegistrering?.let {
                    it.oppvarminger?.map { registrertOppvarming ->
                        Oppvarming(
                            data = registrertOppvarming,
                            metadata = RegisterMetadata(
                                registreringstidspunkt = egenregistrering.first,
                            ),
                        )
                    }
                } ?: emptyList(),
        )
    }
}
