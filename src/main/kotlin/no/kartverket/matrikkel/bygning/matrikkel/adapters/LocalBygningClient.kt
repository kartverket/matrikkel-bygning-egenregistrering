package no.kartverket.matrikkel.bygning.matrikkel.adapters

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Bygning

internal class LocalBygningClient : BygningClient {
    private val bruksenheter: List<Bruksenhet> = listOf(
        Bruksenhet(
            bruksenhetId = 1L,
            bygningId = 1L,
        ),
        Bruksenhet(
            bruksenhetId = 2L,
            bygningId = 1L,
        ),
        Bruksenhet(
            bruksenhetId = 3L,
            bygningId = 2L,
        ),
        Bruksenhet(
            bruksenhetId = 4L,
            bygningId = 2L,
        ),
    )

    private val bygninger: List<Bygning> = listOf(
        Bygning(
            bygningId = 1L,
            bygningNummer = 100L,
            bruksenheter = bruksenheter.subList(0, 2),
        ),
        Bygning(
            bygningId = 2L,
            bygningNummer = 200L,
            bruksenheter = bruksenheter.subList(2, 4),
        ),
    )

    override fun getBygningById(id: Long): Bygning? {
        return bygninger.find { it.bygningId == id }
    }

    override fun getBygningByBygningNummer(bygningNummer: Long): Bygning? {
        return bygninger.find { it.bygningNummer == bygningNummer }
    }
}
