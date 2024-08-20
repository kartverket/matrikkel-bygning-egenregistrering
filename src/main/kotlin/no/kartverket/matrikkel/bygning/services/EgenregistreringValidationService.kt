package no.kartverket.matrikkel.bygning.services

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail

class EgenregistreringValidationService {

    companion object Validator {
        private const val EARLIEST_POSSIBLE_EGENREGISTRERING_YEAR = 1700

        fun validateEgenregistreringRequest(
            egenregistrering: EgenregistreringRequest,
            today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        ): List<ErrorDetail> {

            val bygningRegistreringDates = listOf(
                "avlop" to egenregistrering.bygningsRegistrering.avlop?.metadata?.gyldigFra,
                "bruksareal" to egenregistrering.bygningsRegistrering.bruksareal?.metadata?.gyldigFra,
                "byggeaar" to egenregistrering.bygningsRegistrering.byggeaar?.metadata?.gyldigFra,
                "vannforsyning" to egenregistrering.bygningsRegistrering.vannforsyning?.metadata?.gyldigFra,
            ).mapNotNull { (name, date) ->
                date?.let { name to it }
            }

            val bruksenhetRegistreringDates = egenregistrering.bruksenhetRegistreringer?.map { bruksenhetRegistrering ->
                listOf(
                    "bruksareal" to bruksenhetRegistrering.bruksareal?.metadata?.gyldigFra,
                    "oppvarming" to bruksenhetRegistrering.oppvarming?.metadata?.gyldigFra,
                    "energikilde" to bruksenhetRegistrering.energikilde?.metadata?.gyldigFra,
                ).mapNotNull { (name, date) ->
                    date?.let { name to it }
                }
            }?.flatten()

            val inSixMonths = today.plus(6, DateTimeUnit.MONTH)

            val errorDetails = mutableListOf<ErrorDetail>()

            bygningRegistreringDates.forEach { (field, date) ->
                if (date.year <= EARLIEST_POSSIBLE_EGENREGISTRERING_YEAR) {
                    errorDetails.add(
                        ErrorDetail(
                            pointer = "bygningRegistreringer.$field.metadata",
                            detail = "Gyldighetsdato er satt til å være for langt bak i tid, tidligste mulige registrering er $EARLIEST_POSSIBLE_EGENREGISTRERING_YEAR",
                        ),
                    )
                }

                if (date > inSixMonths) {
                    errorDetails.add(
                        ErrorDetail(
                            pointer = "bygningRegistreringer.$field.metadata",
                            detail = "Gyldighetsdato er satt til å være for langt frem i tid",
                        ),
                    )
                }
            }

            bruksenhetRegistreringDates?.forEachIndexed { index, (field, date) ->
                if (date.year <= EARLIEST_POSSIBLE_EGENREGISTRERING_YEAR) {
                    errorDetails.add(
                        ErrorDetail(
                            pointer = "bruksenhetRegistreringer[$index].$field.metadata",
                            detail = "Gyldighetsdato er satt til å være for langt bak i tid, tidligste mulige registrering er $EARLIEST_POSSIBLE_EGENREGISTRERING_YEAR",
                        ),
                    )
                }

                if (date > inSixMonths) {
                    errorDetails.add(
                        ErrorDetail(
                            pointer = "bruksenhetRegistreringer[$index].$field.metadata",
                            detail = "Gyldighetsdato er satt til å være for langt frem i tid",
                        ),
                    )
                }
            }

            return errorDetails.toList()
        }
    }
}
