package no.kartverket.matrikkel.bygning.services

import io.ktor.server.plugins.requestvalidation.*
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
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

        fun validateEgenregistreringRequest(
            today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        ): suspend (EgenregistreringRequest) -> ValidationResult {
            return { egenregistrering ->
                val bygningRegistreringDates = listOf(
                    "avlop" to egenregistrering.bygningsRegistrering.avlop?.metadata?.gyldigFra,
                    "bruksareal" to egenregistrering.bygningsRegistrering.bruksareal?.metadata?.gyldigFra,
                    "byggeaar" to egenregistrering.bygningsRegistrering.byggeaar?.metadata?.gyldigFra,
                    "vannforsyning" to egenregistrering.bygningsRegistrering.vannforsyning?.metadata?.gyldigFra,
                ).mapNotNull { (name, date) ->
                    date?.let { name to it }
                }

                val bruksenhetRegistreringDates = egenregistrering.bruksenhetRegistreringer.map { bruksenhetRegistrering ->
                    listOf(
                        "bruksareal" to bruksenhetRegistrering.bruksareal?.metadata?.gyldigFra,
                        "oppvarming" to bruksenhetRegistrering.oppvarming?.metadata?.gyldigFra,
                        "energikilde" to bruksenhetRegistrering.energikilde?.metadata?.gyldigFra,
                    ).mapNotNull { (name, date) ->
                        date?.let { name to it }
                    }
                }.flatten()

                val inSixMonths = today.plus(6, DateTimeUnit.MONTH)

                val errors = (bygningRegistreringDates + bruksenhetRegistreringDates).mapNotNull { (field, date) ->
                    // TODO Look into how to format this string to give best status page formatting
                    if (date.year <= EARLIEST_POSSIBLE_EGENREGISTRERING_YEAR) {
                        return@mapNotNull "$field: ${EgenregistreringValidationError.DateTooEarly.errorMessage}"
                    }

                    if (date > inSixMonths) {
                        return@mapNotNull "$field: ${EgenregistreringValidationError.DateTooLate.errorMessage}"
                    }
                    return@mapNotNull null
                }

                if (errors.isNotEmpty()) {
                    ValidationResult.Invalid(errors)
                } else {
                    ValidationResult.Valid
                }
            }
        }
    }

    fun addEgenregistreringToBygning(bygning: Bygning, egenregistrering: EgenregistreringRequest): Boolean {
        val isAllBruksenheterRegisteredOnCorrectBygning = egenregistrering.bruksenhetRegistreringer.any { bruksenhetRegistering ->
            bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId } != null
        }

        if (!isAllBruksenheterRegisteredOnCorrectBygning) return false

        bygningRegistreringer.add(
            BygningsRegistrering(
                bruksareal = egenregistrering.bygningsRegistrering.bruksareal,
                byggeaar = egenregistrering.bygningsRegistrering.byggeaar,
                vannforsyning = egenregistrering.bygningsRegistrering.vannforsyning,
                avlop = egenregistrering.bygningsRegistrering.avlop,
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
