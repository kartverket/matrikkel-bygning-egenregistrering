package clients

import matrikkelclients.BygningClient
import models.Bruksenhet
import models.Bygning

// Burde denne leve i matrikkel-factory eller matrikkel-api?
class LocalBygningClient : BygningClient {
    private val bruksenheter: List<Bruksenhet> = listOf(
        Bruksenhet(
            1L, 1L
        ), Bruksenhet(
            2L, 1L
        ), Bruksenhet(
            3L, 2L
        ), Bruksenhet(
            4L, 2L
        )
    )

    private val bygninger: List<Bygning> = listOf(
        Bygning(
            1L, 100L, bruksenheter = bruksenheter.subList(0, 2)
        ), Bygning(
            2L, 200L, bruksenheter = bruksenheter.subList(2, 4)
        )
    )

    override fun getBygningById(id: Long): Bygning? {
        return bygninger.find { it.bygningId == id }
    }

    override fun getBygningByBygningNummer(bygningNummer: Long): Bygning? {
        return bygninger.find { it.bygningNummer == bygningNummer }
    }
}