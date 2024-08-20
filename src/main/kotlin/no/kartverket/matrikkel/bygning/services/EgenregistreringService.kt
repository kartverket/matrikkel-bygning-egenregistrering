package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.requests.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringRequest
import java.util.*

class EgenregistreringService {
    private val bygningRegistreringer: MutableList<BygningRegistrering> = mutableListOf()
    private val bruksenhetRegistreringer: MutableList<BruksenhetRegistrering> = mutableListOf()

    fun addEgenregistreringToBygning(bygning: Bygning, egenregistrering: EgenregistreringRequest): Boolean {
        val isAllBruksenheterRegisteredOnCorrectBygning = egenregistrering.bruksenhetRegistreringer.any { bruksenhetRegistering ->
            bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId } != null
        }

        if (!isAllBruksenheterRegisteredOnCorrectBygning) return false

        // TODO Bør kunne skille alle registreringer med en ID, skal denne settes på metadataen til hver enkelt registrering?
        val egenregistreringsId = UUID.randomUUID().toString()

        bygningRegistreringer.add(
            BygningRegistrering(
                bruksareal = egenregistrering.bygningRegistrering.bruksareal,
                byggeaar = egenregistrering.bygningRegistrering.byggeaar,
                vannforsyning = egenregistrering.bygningRegistrering.vannforsyning,
                avlop = egenregistrering.bygningRegistrering.avlop,
            ),
        )

        egenregistrering.bruksenhetRegistreringer.forEach { bruksenhetRegistrering ->
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
