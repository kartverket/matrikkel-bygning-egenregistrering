package no.kartverket.matrikkel.bygning.services

import kotlinx.datetime.*
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.requests.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BygningsRegistrering
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringRequest

class EgenregistreringsService {
    private val bygningRegistreringer: MutableList<BygningsRegistrering> = mutableListOf()
    private val bruksenhetRegistreringer: MutableList<BruksenhetRegistrering> = mutableListOf()

    fun addEgenregistreringToBygning(bygning: Bygning, egenregistrering: EgenregistreringRequest): Boolean {
        val isAllBruksenheterRegisteredOnCorrectBygning =
            egenregistrering.bruksenhetRegistreringer.any { bruksenhetRegistering ->
                bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId } != null
            }

        if (!isAllBruksenheterRegisteredOnCorrectBygning) return false

        bygningRegistreringer.add(
            BygningsRegistrering(
                bruksareal = egenregistrering.bygningsRegistrering.bruksareal,
                byggeaar = egenregistrering.bygningsRegistrering.byggeaar,
                vannforsyning = egenregistrering.bygningsRegistrering.vannforsyning,
                avlop = egenregistrering.bygningsRegistrering.avlop,
            )
        )

        egenregistrering.bruksenhetRegistreringer.forEach { bruksenhetRegistrering ->
            bruksenhetRegistreringer.add(
                BruksenhetRegistrering(
                    bruksenhetId = bruksenhetRegistrering.bruksenhetId,
                    bruksareal = bruksenhetRegistrering.bruksareal,
                    energikilde = bruksenhetRegistrering.energikilde,
                    oppvarming = bruksenhetRegistrering.oppvarming,
                )
            )
        }

        return true
    }

    fun validateEgenregistrering(egenregistrering: EgenregistreringRequest): Boolean {
        val bygningRegistreringDates = listOfNotNull(
            egenregistrering.bygningsRegistrering.avlop?.metadata?.gyldigFra,
            egenregistrering.bygningsRegistrering.bruksareal?.metadata?.gyldigFra,
            egenregistrering.bygningsRegistrering.byggeaar?.metadata?.gyldigFra,
            egenregistrering.bygningsRegistrering.vannforsyning?.metadata?.gyldigFra
        )

        val bruksenhetRegistreringDates = egenregistrering.bruksenhetRegistreringer.map {
            listOfNotNull(
                it.bruksareal?.metadata?.gyldigFra,
                it.oppvarming?.metadata?.gyldigFra,
                it.energikilde?.metadata?.gyldigFra
            )
        }.flatten()

        val isAnyRegistreringsDateInvalid = (bygningRegistreringDates + bruksenhetRegistreringDates).any {
            val earliestPossibleValidYear = 1700
            // Er vi bænkers på system tidssone på SKIP?
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

            // TODO Jeg har bare gjetta på et tall her, godt mulig det bør være tillatt å fremtidsføre lenger frem i tid enn dette?
            val inSixMonths = today.plus(6, DateTimeUnit.MONTH)

            return (it.year <= earliestPossibleValidYear || it > inSixMonths)
        }

        return isAnyRegistreringsDateInvalid
    }
}