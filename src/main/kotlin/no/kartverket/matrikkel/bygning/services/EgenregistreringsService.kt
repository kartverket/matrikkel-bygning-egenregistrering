package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Egenregistrering

class EgenregistreringsService {
    val bygninger: MutableList<Bygning> = mutableListOf(
        Bygning("1", mutableListOf()),
        Bygning("2", mutableListOf()),
    )
    val bruksenheter: MutableList<Bruksenhet> = mutableListOf(
        Bruksenhet("a", "1", mutableListOf()),
        Bruksenhet("b", "1", mutableListOf()),
        Bruksenhet("c", "2", mutableListOf()),
        Bruksenhet("d", "2", mutableListOf()),
    )

    fun addEgenregistreringToBygning(egenregistrering: Egenregistrering): Boolean {
        val bygningIdForEgenregistrering = egenregistrering.bygningsRegistrering.bygningsId

        val isAllBruksenheterRegisteredOnCorrectBygning = egenregistrering.bruksenhetRegistreringer.any { it ->
            val bruksenhet = bruksenheter.find { bruksenhet -> it.bruksenhetId == bruksenhet.bygningId }

            return bruksenhet?.bygningId != bygningIdForEgenregistrering
        }

        if (!isAllBruksenheterRegisteredOnCorrectBygning) return false

        val bygningIfExists = bygninger.find { it.id == bygningIdForEgenregistrering }

        if (bygningIfExists == null) {
            bygninger.add(
                Bygning(
                    id = bygningIdForEgenregistrering,
                    egenregistreringer = mutableListOf(egenregistrering.bygningsRegistrering)
                )
            )
        } else {
            bygningIfExists.egenregistreringer.add(egenregistrering.bygningsRegistrering)
        }

        egenregistrering.bruksenhetRegistreringer.forEach { bruksenhetRegistrering ->
            val bruksenhetId = bruksenhetRegistrering.bruksenhetId
            val bruksenhetIfExists = bruksenheter.find { it.id == bruksenhetId }

            if (bruksenhetIfExists == null) {
                bruksenheter.add(
                    Bruksenhet(
                        id = bruksenhetId,
                        bygningId = bygningIdForEgenregistrering,
                        egenregistreringer = mutableListOf(bruksenhetRegistrering)
                    )
                )
            } else {
                bruksenhetIfExists.egenregistreringer.add(bruksenhetRegistrering)
            }
        }

        return true
    }
}