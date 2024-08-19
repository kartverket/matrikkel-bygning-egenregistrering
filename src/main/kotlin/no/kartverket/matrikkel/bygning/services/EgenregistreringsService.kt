package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.requests.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BygningsRegistrering
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringRequest
import java.util.*

class EgenregistreringsService {
    private val bygningRegistreringer: MutableList<BygningsRegistrering> = mutableListOf()
    private val bruksenhetRegistreringer: MutableList<BruksenhetRegistrering> = mutableListOf()

    fun addEgenregistreringToBygning(bygning: Bygning, egenregistrering: EgenregistreringRequest): Boolean {
        if (egenregistrering.bruksenhetRegistreringer?.isNotEmpty() == true) {
            val isAllBruksenheterRegisteredOnCorrectBygning = egenregistrering.bruksenhetRegistreringer.any { bruksenhetRegistering ->
                bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId } != null
            }

            if (!isAllBruksenheterRegisteredOnCorrectBygning) return false
        }

        // TODO Bør kunne skille alle registreringer med en ID, skal denne settes på metadataen til hver enkelt registrering?
        val egenregistreringsId = UUID.randomUUID().toString()

        bygningRegistreringer.add(
            BygningsRegistrering(
                bruksareal = egenregistrering.bygningsRegistrering.bruksareal,
                byggeaar = egenregistrering.bygningsRegistrering.byggeaar,
                vannforsyning = egenregistrering.bygningsRegistrering.vannforsyning,
                avlop = egenregistrering.bygningsRegistrering.avlop,
            ),
        )

        egenregistrering.bruksenhetRegistreringer?.forEach { bruksenhetRegistrering ->
            bruksenhetRegistreringer.add(
                BruksenhetRegistrering(
                    bruksenhetId = bruksenhetRegistrering.bruksenhetId,
                    bruksareal = bruksenhetRegistrering.bruksareal,
                    energikilde = bruksenhetRegistrering.energikilde,
                    oppvarming = bruksenhetRegistrering.oppvarming,
                ),
            )
        }

        return true
    }
}
