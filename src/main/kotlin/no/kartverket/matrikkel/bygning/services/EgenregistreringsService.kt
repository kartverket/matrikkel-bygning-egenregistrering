package no.kartverket.matrikkel.bygning.services

import kotlinx.datetime.*
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.requests.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BygningsRegistrering
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringValidationError

class EgenregistreringsService {
    private val bygningRegistreringer: MutableList<BygningsRegistrering> = mutableListOf()
    private val bruksenhetRegistreringer: MutableList<BruksenhetRegistrering> = mutableListOf()

    companion object Validator {
        const val EARLIEST_POSSIBLE_EGENREGISTRERING_YEAR = 1700

        fun validateEgenregistrering(
            egenregistrering: EgenregistreringRequest,
            today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        ): List<Pair<String, EgenregistreringValidationError>> {
            val bygningRegistreringDates = listOf(
                "avlop" to egenregistrering.bygningsRegistrering.avlop?.metadata?.gyldigFra,
                "bruksareal" to egenregistrering.bygningsRegistrering.bruksareal?.metadata?.gyldigFra,
                "byggeaar" to egenregistrering.bygningsRegistrering.byggeaar?.metadata?.gyldigFra,
                "vannforsyning" to egenregistrering.bygningsRegistrering.vannforsyning?.metadata?.gyldigFra
            ).mapNotNull { (name, date) ->
                date?.let { name to it }
            }

            val bruksenhetRegistreringDates = egenregistrering.bruksenhetRegistreringer.map { bruksenhetRegistrering ->
                listOf(
                    "bruksareal" to bruksenhetRegistrering.bruksareal?.metadata?.gyldigFra,
                    "oppvarming" to bruksenhetRegistrering.oppvarming?.metadata?.gyldigFra,
                    "energikilde" to bruksenhetRegistrering.energikilde?.metadata?.gyldigFra
                ).mapNotNull { (name, date) ->
                    date?.let { name to it }
                }
            }.flatten()

            // TODO Jeg har bare gjetta på et tall her, godt mulig det bør være tillatt å fremtidsføre lenger frem i tid enn dette?
            val inSixMonths = today.plus(6, DateTimeUnit.MONTH)

            return (bygningRegistreringDates + bruksenhetRegistreringDates).mapNotNull {
                if (it.second.year <= EARLIEST_POSSIBLE_EGENREGISTRERING_YEAR) {
                    return@mapNotNull it.first to EgenregistreringValidationError.DateTooLate
                }

                if (it.second > inSixMonths) {
                    return@mapNotNull it.first to EgenregistreringValidationError.DateTooEarly
                }

                return@mapNotNull null
            }
        }
    }

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

}