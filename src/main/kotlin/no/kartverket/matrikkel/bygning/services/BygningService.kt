package no.kartverket.matrikkel.bygning.services

import kotlinx.datetime.LocalDate
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.BruksenhetStorage
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.BygningStorage
import no.kartverket.matrikkel.bygning.repositories.BygningRepository

class BygningService(val bygningRepository: BygningRepository) {
    val bruksenhetStorage: MutableList<BruksenhetStorage> = mutableListOf(
        BruksenhetStorage("a", "1", mutableListOf(), mutableListOf(), mutableListOf()),
        BruksenhetStorage("b", "1", mutableListOf(), mutableListOf(), mutableListOf()),
        BruksenhetStorage("c", "2", mutableListOf(), mutableListOf(), mutableListOf()),
        BruksenhetStorage("d", "2", mutableListOf(), mutableListOf(), mutableListOf()),
    )

    val bygningStorage: MutableList<BygningStorage> = mutableListOf(
        BygningStorage("1", mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()),
        BygningStorage("2", mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()),
    )

    fun getBygning(bygningId: String, gyldigFra: LocalDate?): Bygning? {
        // TODO Filtrer ut egenregistreringer uten en gitt gyldighetsdato

        val bygning = bygningStorage.find { bygningId == it.bygningId } ?: return null

        val bruksenheter = bruksenhetStorage.filter { bygningId == it.bygningId }.map {
            Bruksenhet(
                it.bruksenhetId,
                it.bygningId,
                it.bruksarealRegistreringer,
                it.energikildeRegistreringer,
                it.oppvarmingRegistreringer
            )
        }

        return Bygning(
            bygningId = bygning.bygningId,
            bruksenheter = bruksenheter,
            bruksarealRegistreringer = bygning.bruksarealRegistreringer,
            byggeaarRegistreringer = bygning.byggeaarRegistreringer,
            vannforsyningsRegistreringer = bygning.vannforsyningsRegistreringer,
            avlopRegistreringer = bygning.avlopRegistreringer,
        )
    }
}
