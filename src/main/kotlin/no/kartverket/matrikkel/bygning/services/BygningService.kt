package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Energikilde
import no.kartverket.matrikkel.bygning.models.Oppvarming

class BygningService {
    private val bygninger: MutableList<Bygning> = mutableListOf(
        Bygning(
            id = "123",
            areal = 10.0,
            avlop = true,
            energikilder = listOf(Energikilde.NOE),
            vann = true,
            byggeaar = 1998,
            oppvarming = listOf(Oppvarming.VARMEPUMPE)
        ),
    )

    fun addBygning(bygning: Bygning): Boolean {
        bygninger.add(bygning)
        return true
    }

    fun getBygninger(): List<Bygning> {
        return bygninger.toList()
    }
}