package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Egenregistrering

class EgenregistreringsService {
    val bygninger: MutableList<Bygning> = mutableListOf(
        Bygning("1", mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()),
        Bygning("2", mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()),
    )
    val bruksenheter: MutableList<Bruksenhet> = mutableListOf(
        Bruksenhet("a", "1", mutableListOf(), mutableListOf(), mutableListOf()),
        Bruksenhet("b", "1", mutableListOf(), mutableListOf(), mutableListOf()),
        Bruksenhet("c", "2", mutableListOf(), mutableListOf(), mutableListOf()),
        Bruksenhet("d", "2", mutableListOf(), mutableListOf(), mutableListOf()),
    )

    fun addEgenregistreringToBygning(bygningId: String, egenregistrering: Egenregistrering): Boolean {
        val isAllBruksenheterRegisteredOnCorrectBygning = egenregistrering.bruksenhetRegistreringer.any {
            val bruksenhet = bruksenheter.find { bruksenhet -> it.bruksenhetId == bruksenhet.bygningId }

            bruksenhet?.bygningId != bygningId
        }

        if (!isAllBruksenheterRegisteredOnCorrectBygning) return false

        val bygningIfExists = bygninger.find { it.id == bygningId }

        if (bygningIfExists == null) {
            bygninger.add(
                Bygning(
                    id = bygningId,
                    bruksarealRegistreringer = mutableListOf(egenregistrering.bygningsRegistrering.bruksarealRegistrering),
                    byggeaarRegistreringer = mutableListOf(egenregistrering.bygningsRegistrering.byggeaarRegistrering),
                    vannforsyningsRegistreringer = mutableListOf(egenregistrering.bygningsRegistrering.vannforsyningsRegistrering),
                    avlopRegistreringer = mutableListOf(egenregistrering.bygningsRegistrering.avlopRegistrering)
                )
            )
        } else {
            bygningIfExists.bruksarealRegistreringer.add(egenregistrering.bygningsRegistrering.bruksarealRegistrering)
            bygningIfExists.byggeaarRegistreringer.add(egenregistrering.bygningsRegistrering.byggeaarRegistrering)
            bygningIfExists.vannforsyningsRegistreringer.add(egenregistrering.bygningsRegistrering.vannforsyningsRegistrering)
            bygningIfExists.avlopRegistreringer.add(egenregistrering.bygningsRegistrering.avlopRegistrering)
        }

        egenregistrering.bruksenhetRegistreringer.forEach { bruksenhetRegistrering ->
            val bruksenhetId = bruksenhetRegistrering.bruksenhetId
            val bruksenhetIfExists = bruksenheter.find { it.id == bruksenhetId }

            if (bruksenhetIfExists == null) {
                bruksenheter.add(
                    Bruksenhet(
                        id = bruksenhetId,
                        bygningId = bygningId,
                        bruksarealRegistreringer = mutableListOf(bruksenhetRegistrering.bruksarealRegistrering),
                        energikildeRegistreringer = mutableListOf(bruksenhetRegistrering.energikildeRegistrering),
                        oppvarmingRegistreringer = mutableListOf(bruksenhetRegistrering.oppvarmingRegistrering),
                    )
                )
            } else {
                bruksenhetIfExists.bruksarealRegistreringer.add(bruksenhetRegistrering.bruksarealRegistrering)
                bruksenhetIfExists.energikildeRegistreringer.add(bruksenhetRegistrering.energikildeRegistrering)
                bruksenhetIfExists.oppvarmingRegistreringer.add(bruksenhetRegistrering.oppvarmingRegistrering)
            }
        }

        return true
    }

    fun getEgenregistreringerForBygning(bygningId: String, gyldigFra: Long): Bygning? {
        val bygning = bygninger.find { it.id == bygningId } ?: return null

        return bygning.copy(
            bruksarealRegistreringer = bygning.bruksarealRegistreringer.filter { it.registreringMetadata.gyldigFra > gyldigFra }
                .toMutableList(),
            byggeaarRegistreringer = bygning.byggeaarRegistreringer.filter { it.registreringMetadata.gyldigFra > gyldigFra }
                .toMutableList(),
            vannforsyningsRegistreringer = bygning.vannforsyningsRegistreringer.filter { it.registreringMetadata.gyldigFra > gyldigFra }
                .toMutableList(),
            avlopRegistreringer = bygning.avlopRegistreringer.filter { it.registreringMetadata.gyldigFra > gyldigFra }
                .toMutableList(),
        )
    }
}