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
            byggeaar = bygningAggregate.byggeaar ?: if (egenregistrering.bygningRegistrering.byggeaarRegistrering != null) {
                Byggeaar(
                    data = egenregistrering.bygningRegistrering.byggeaarRegistrering.byggeaar,
                    metadata = RegisterMetadata(
                        registreringstidspunkt = egenregistrering.registreringstidspunkt,
                    ),
                )
            } else null,
            bruksareal = bygningAggregate.bruksareal ?: if (egenregistrering.bygningRegistrering.bruksarealRegistrering != null) {
                Bruksareal(
                    data = egenregistrering.bygningRegistrering.bruksarealRegistrering.bruksareal,
                    metadata = RegisterMetadata(
                        registreringstidspunkt = egenregistrering.registreringstidspunkt,
                    ),
                )
            } else null,
            vannforsyning = bygningAggregate.vannforsyning
                ?: if (egenregistrering.bygningRegistrering.vannforsyningRegistrering != null) {
                    Vannforsyning(
                        data = egenregistrering.bygningRegistrering.vannforsyningRegistrering.vannforsyning,
                        metadata = RegisterMetadata(
                            registreringstidspunkt = egenregistrering.registreringstidspunkt,
                        ),
                    )
                } else null,
            avlop = bygningAggregate.avlop ?: if (egenregistrering.bygningRegistrering.avlopRegistrering != null) {
                Avlop(
                    data = egenregistrering.bygningRegistrering.avlopRegistrering.avlop,
                    metadata = RegisterMetadata(
                        registreringstidspunkt = egenregistrering.registreringstidspunkt,
                    ),
                )
            } else null,
        )
    }
}


fun Bruksenhet.withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bruksenhet {
    // Her så antar jeg at man bare har registrert bruksenheten én gang per registrering, som gir mening
    // Likevel har vi ikke noen logisk sjekk på dette ved registrering, så det bør vi nok ha
    val bruksenhetRegistreringer = egenregistreringer.mapNotNull { egenregistrering ->
        val bruksenhetRegistrering =
            egenregistrering.bygningRegistrering.bruksenhetRegistreringer.filter { it.bruksenhetId == this.bruksenhetId }
                .firstOrNull()

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
            bruksareal = bruksenhetAggregate.bruksareal ?: if (egenregistrering.second.bruksarealRegistrering != null) {
                Bruksareal(
                    // ser ikke ut som kotlin skjønner at bruksarealRegistrering ikke er null her
                    data = egenregistrering.second.bruksarealRegistrering!!.bruksareal,
                    metadata = RegisterMetadata(
                        registreringstidspunkt = egenregistrering.first,
                    ),
                )
            } else null,
            energikilder = bruksenhetAggregate.energikilder.takeIf { it.isNotEmpty() }
                ?: if (egenregistrering.second.energikildeRegistrering?.energikilder?.isNotEmpty() == true) {
                    egenregistrering.second.energikildeRegistrering!!.energikilder.map { registrertKilde ->
                        Energikilde(
                            data = registrertKilde,
                            metadata = RegisterMetadata(
                                registreringstidspunkt = egenregistrering.first,
                            ),
                        )
                    }
                } else emptyList(),
            oppvarminger = bruksenhetAggregate.oppvarminger.takeIf { it.isNotEmpty() }
                ?: if (egenregistrering.second.oppvarmingRegistrering?.oppvarminger?.isNotEmpty() == true) {
                    egenregistrering.second.oppvarmingRegistrering!!.oppvarminger.map { registrertOppvarming ->
                        Oppvarming(
                            data = registrertOppvarming,
                            metadata = RegisterMetadata(
                                registreringstidspunkt = egenregistrering.first,
                            ),
                        )
                    }
                } else emptyList(),
        )
    }
}
