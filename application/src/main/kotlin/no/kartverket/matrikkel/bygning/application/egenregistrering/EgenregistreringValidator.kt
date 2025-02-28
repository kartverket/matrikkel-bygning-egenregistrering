package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.MultipleValidationError
import no.kartverket.matrikkel.bygning.application.models.error.ValidationError

class EgenregistreringValidator {
    companion object {
        fun validateEgenregistrering(egenregistrering: Egenregistrering): Result<Unit, MultipleValidationError> {
            val errors = listOfNotNull(
                validateRepeatedBruksenheter(egenregistrering),
            )
                .plus(validateBruksarealRegistreringerTotaltArealIsEqualEtasjerIfExists(egenregistrering))

            return if (errors.isEmpty()) {
                Ok(Unit)
            } else {
                Err(MultipleValidationError(errors))
            }
        }

        private fun validateRepeatedBruksenheter(egenregistrering: Egenregistrering): ValidationError? {
            val repeatedBruksenheter =
                egenregistrering.bruksenhetRegistreringer.groupBy { it.bruksenhetBubbleId }.filter { it.value.size > 1 }
                    .map { it.key }


            if (repeatedBruksenheter.isNotEmpty()) {
                return ValidationError(
                    message = "Bruksenhet ${if (repeatedBruksenheter.size > 1) "er" else ""} med ID ${repeatedBruksenheter.joinToString()} har flere registreringer på seg. Kun én registrering per bruksenhet kan sendes inn",
                )
            }

            return null
        }

        private fun validateBruksarealRegistreringerTotaltArealIsEqualEtasjerIfExists(egenregistrering: Egenregistrering): List<ValidationError> {
            return egenregistrering.bruksenhetRegistreringer
                .filter {
                    it.bruksarealRegistrering?.isTotaltBruksarealEqualTotaltEtasjeArealIfSet() == false
                }
                .map { invalidRegistrering ->
                    ValidationError(
                        message = "Bruksenhet med ID ${invalidRegistrering.bruksenhetBubbleId} har registrert totalt BRA og BRA per etasje, men totalt BRA stemmer ikke overens med totalen av BRA per etasje",
                    )
                }

        }
    }
}
