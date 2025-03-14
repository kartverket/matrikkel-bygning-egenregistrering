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
                validateBruksarealRegistreringerTotaltArealIsEqualEtasjerIfExists(egenregistrering),
            )

            return if (errors.isEmpty()) {
                Ok(Unit)
            } else {
                Err(MultipleValidationError(errors))
            }
        }

        private fun validateBruksarealRegistreringerTotaltArealIsEqualEtasjerIfExists(egenregistrering: Egenregistrering): ValidationError? {
            return if (egenregistrering.bruksenhetRegistrering.bruksarealRegistrering?.isTotaltBruksarealEqualTotaltEtasjeArealIfSet() == false) {
                ValidationError(
                    message = "Bruksenhet med ID ${egenregistrering.bruksenhetRegistrering.bruksenhetBubbleId.value} har registrert totalt BRA og BRA per etasje, men totalt BRA stemmer ikke overens med totalen av BRA per etasje",
                )
            } else {
                null
            }
        }
    }
}
