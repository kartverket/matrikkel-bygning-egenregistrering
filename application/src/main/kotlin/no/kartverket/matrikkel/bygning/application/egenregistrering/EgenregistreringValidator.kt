package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.MultipleValidationError
import no.kartverket.matrikkel.bygning.application.models.error.ValidationError

class EgenregistreringValidator {
    companion object {
        fun validateEgenregistrering(egenregistrering: Egenregistrering, bygning: Bygning): Result<Unit, MultipleValidationError> {
            val errors = listOfNotNull(
                validateBruksenheterRegistreredOnCorrectBygning(egenregistrering, bygning),
                validateRepeatedBruksenheter(egenregistrering),
            )
                .plus(validateBruksarealRegistreringerTotaltArealIsEqualEtasjerIfExists(egenregistrering))

            return if (errors.isEmpty()) {
                Ok(Unit)
            } else {
                Err(MultipleValidationError(errors))
            }
        }

        private fun validateBruksenheterRegistreredOnCorrectBygning(
            egenregistrering: Egenregistrering, bygning: Bygning
        ): ValidationError? {
            val invalidBruksenheter = egenregistrering.bygningRegistrering.bruksenhetRegistreringer.mapNotNull { bruksenhetRegistering ->
                val bruksenhet = bygning.bruksenheter.find { it.bruksenhetId.value == bruksenhetRegistering.bruksenhetId }
                if (bruksenhet == null) {
                    bruksenhetRegistering.bruksenhetId
                } else {
                    null
                }
            }

            if (invalidBruksenheter.isNotEmpty()) {
                return ValidationError(
                    message = "Bruksenhet${if (invalidBruksenheter.size > 1) "er" else ""} med ID ${invalidBruksenheter.joinToString()} finnes ikke i bygning med ID ${bygning.bygningId}",
                )
            }

            return null
        }

        private fun validateRepeatedBruksenheter(egenregistrering: Egenregistrering): ValidationError? {
            val repeatedBruksenheter =
                egenregistrering.bygningRegistrering.bruksenhetRegistreringer.groupBy { it.bruksenhetId }.filter { it.value.size > 1 }
                    .map { it.key }


            if (repeatedBruksenheter.isNotEmpty()) {
                return ValidationError(
                    message = "Bruksenhet ${if (repeatedBruksenheter.size > 1) "er" else ""} med ID ${repeatedBruksenheter.joinToString()} har flere registreringer på seg. Kun én registrering per bruksenhet kan sendes inn",
                )
            }

            return null
        }

        private fun validateBruksarealRegistreringerTotaltArealIsEqualEtasjerIfExists(egenregistrering: Egenregistrering): List<ValidationError> {
            return egenregistrering.bygningRegistrering.bruksenhetRegistreringer
                .filter {
                    it.bruksarealRegistrering?.isTotaltBruksarealEqualTotaltEtasjeArealIfSet() == false
                }
                .map { invalidRegistrering ->
                    ValidationError(
                        message = "Bruksenhet med ID ${invalidRegistrering.bruksenhetId} har registrert totalt BRA og BRA per etasje, men totalt BRA stemmer ikke overens med totalen av BRA per etasje",
                    )
                }

        }
    }
}
