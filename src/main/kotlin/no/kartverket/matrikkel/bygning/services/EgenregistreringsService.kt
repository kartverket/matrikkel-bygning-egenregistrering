package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.models.BruksenhetStorage
import no.kartverket.matrikkel.bygning.models.BygningStorage
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringRequest
import java.util.*

class EgenregistreringsService(private val bygningService: BygningService) {

    fun addEgenregistreringToBygning(bygningId: String, egenregistrering: EgenregistreringRequest): Boolean {
        val bygningIfExists = bygningService.bygningStorage.find { it.bygningId == bygningId }

        val isAllBruksenheterRegisteredOnCorrectBygning =
            egenregistrering.bruksenhetRegistreringer.any { bruksenhetRegistering ->
                val bruksenhetIfExists =
                    bygningService.bruksenhetStorage.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId }

                bruksenhetIfExists?.bygningId == bygningId
            }

        if (!isAllBruksenheterRegisteredOnCorrectBygning) return false

        // TODO Bør kunne skille alle registreringer med en ID, skal denne settes på metadataen til hver enkelt registrering?
        val egenregistreringsId = UUID.randomUUID().toString()

        if (bygningIfExists == null) {
            bygningService.bygningStorage.add(BygningStorage(
                bygningId = bygningId,
                bruksarealRegistreringer = egenregistrering.bygningsRegistrering.bruksareal?.let { mutableListOf(it) }
                    ?: mutableListOf(),
                byggeaarRegistreringer = egenregistrering.bygningsRegistrering.byggeaar?.let { mutableListOf(it) }
                    ?: mutableListOf(),
                vannforsyningsRegistreringer = egenregistrering.bygningsRegistrering.vannforsyning?.let {
                    mutableListOf(
                        it
                    )
                } ?: mutableListOf(),
                avlopRegistreringer = egenregistrering.bygningsRegistrering.avlop?.let { mutableListOf(it) }
                    ?: mutableListOf(),
            ))
        } else {
            egenregistrering.bygningsRegistrering.bruksareal?.let { bygningIfExists.bruksarealRegistreringer.add(it) }
            egenregistrering.bygningsRegistrering.byggeaar?.let { bygningIfExists.byggeaarRegistreringer.add(it) }
            egenregistrering.bygningsRegistrering.vannforsyning?.let {
                bygningIfExists.vannforsyningsRegistreringer.add(
                    it
                )
            }
            egenregistrering.bygningsRegistrering.avlop?.let { bygningIfExists.avlopRegistreringer.add(it) }
        }

        egenregistrering.bruksenhetRegistreringer.forEach { bruksenhetRegistrering ->
            val bruksenhetId = bruksenhetRegistrering.bruksenhetId
            val bruksenhetIfExists = bygningService.bruksenhetStorage.find { it.bruksenhetId == bruksenhetId }

            if (bruksenhetIfExists == null) {
                bygningService.bruksenhetStorage.add(BruksenhetStorage(
                    bruksenhetId = bruksenhetId,
                    bygningId = bygningId,
                    bruksarealRegistreringer = bruksenhetRegistrering.bruksareal?.let { mutableListOf(it) }
                        ?: mutableListOf(),
                    energikildeRegistreringer = bruksenhetRegistrering.energikilde?.let { mutableListOf(it) }
                        ?: mutableListOf(),
                    oppvarmingRegistreringer = bruksenhetRegistrering.oppvarming?.let { mutableListOf(it) }
                        ?: mutableListOf(),
                ))
            } else {
                bruksenhetRegistrering.bruksareal?.let { bruksenhetIfExists.bruksarealRegistreringer.add(it) }
                bruksenhetRegistrering.energikilde?.let { bruksenhetIfExists.energikildeRegistreringer.add(it) }
                bruksenhetRegistrering.oppvarming?.let { bruksenhetIfExists.oppvarmingRegistreringer.add(it) }
            }
        }

        return true
    }
}