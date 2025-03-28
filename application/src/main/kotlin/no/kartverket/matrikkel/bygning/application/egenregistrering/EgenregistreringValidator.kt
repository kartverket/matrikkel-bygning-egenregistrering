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
            val errors =
                listOfNotNull(
                    validateBruksarealRegistreringerTotaltArealIsEqualEtasjerIfExists(egenregistrering),
                    validateListeregistreringDuplicates(egenregistrering),
                )

            return if (errors.isEmpty()) {
                Ok(Unit)
            } else {
                Err(MultipleValidationError(errors))
            }
        }

        private fun validateBruksarealRegistreringerTotaltArealIsEqualEtasjerIfExists(
            egenregistrering: Egenregistrering,
        ): ValidationError? =
            if (egenregistrering.bruksenhetRegistrering.bruksarealRegistrering?.checkIsTotaltBruksarealEqualTotaltEtasjeArealIfSet() ==
                false
            ) {
                ValidationError(
                    message = @Suppress("ktlint:standard:max-line-length")
                    "Bruksenhet med ID ${egenregistrering.bruksenhetRegistrering.bruksenhetBubbleId.value} har registrert totalt BRA og BRA per etasje, men totalt BRA stemmer ikke overens med totalen av BRA per etasje",
                )
            } else {
                null
            }

        private fun validateListeregistreringDuplicates(egenregistrering: Egenregistrering): ValidationError? {
            val oppvarmingHasDuplicate =
                egenregistrering.bruksenhetRegistrering.oppvarmingRegistrering.hasDuplicateElements { it.oppvarming }
            val energikilderHasDuplicate =
                egenregistrering.bruksenhetRegistrering.energikildeRegistrering.hasDuplicateElements { it.energikilde }

            return if (oppvarmingHasDuplicate || energikilderHasDuplicate) {
                ValidationError(
                    message = @Suppress("ktlint:standard:max-line-length")
                    "Bruksenhet med ID ${egenregistrering.bruksenhetRegistrering.bruksenhetBubbleId.value} har dupliserte registreringer i oppvarming eller energikilder",
                )
            } else {
                null
            }
        }

        private fun <T> List<T>?.hasDuplicateElements(keySelector: (T) -> Any): Boolean =
            this?.groupBy(keySelector)?.any { it.value.size > 1 } == true
    }
}
