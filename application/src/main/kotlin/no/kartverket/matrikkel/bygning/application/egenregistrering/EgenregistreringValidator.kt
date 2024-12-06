package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.HasKildemateriale
import no.kartverket.matrikkel.bygning.application.models.error.MultipleValidationError
import no.kartverket.matrikkel.bygning.application.models.error.ValidationError
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode

class EgenregistreringValidator {
    companion object {
        fun validateEgenregistrering(egenregistrering: Egenregistrering, bygning: Bygning): Result<Unit, MultipleValidationError> {
            val errors = listOfNotNull(
                validateBruksenheterRegistreredOnCorrectBygning(egenregistrering, bygning),
                validateRepeatedBruksenheter(egenregistrering),
            ) + validateKildemateriale(egenregistrering) + validateBruksarealRegistreringerTotaltAreal(egenregistrering)

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
                val bruksenhet = bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId }

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

        private fun validateBruksarealRegistreringerTotaltAreal(egenregistrering: Egenregistrering): List<ValidationError> {
            val invalidBruksarealRegistreringer = egenregistrering.bygningRegistrering.bruksenhetRegistreringer
                .filter {
                    it.bruksarealRegistrering?.isTotaltBruksarealEqualTotaltEtasjeArealIfSet() == false
                }

            return invalidBruksarealRegistreringer.map { invalidRegistrering ->
                ValidationError(
                    message = "Bruksenhet med ID ${invalidRegistrering.bruksenhetId} har registrert totalt bruksareal og areal per etasje, men det totale bruksarealet stemmer ikke overens med totalen av BRA per etasje",
                )
            }
        }

        private fun validateKildemateriale(egenregistrering: Egenregistrering): List<ValidationError> {
            return egenregistrering.bygningRegistrering.bruksenhetRegistreringer.flatMap { bruksenhetRegistrering ->
                validateKildematerialeForBruksenhetRegistrering(bruksenhetRegistrering)
            }
        }

        private fun validateKildematerialeForBruksenhetRegistrering(bruksenhetRegistrering: BruksenhetRegistrering): List<ValidationError> {
            return listOfNotNull(
                bruksenhetRegistrering.avlopRegistrering,
                bruksenhetRegistrering.byggeaarRegistrering,
                bruksenhetRegistrering.oppvarmingRegistrering,
                bruksenhetRegistrering.energikildeRegistrering,
                bruksenhetRegistrering.vannforsyningRegistrering,
            ).mapNotNull {
                if (!isValidKildemateriale(it)) {
                    ValidationError(
                        message = "${it::class.simpleName} (Bruksenhet ID: ${bruksenhetRegistrering.bruksenhetId}) har feil kildemateriale: ${it.kildemateriale}.",
                    )
                } else {
                    null
                }
            }
        }

        private fun isValidKildemateriale(egenregistreringFelt: HasKildemateriale): Boolean {
            if (egenregistreringFelt.kildemateriale == null) {
                return false
            }

            return when (egenregistreringFelt) {
                is BruksarealRegistrering -> egenregistreringFelt.kildemateriale in listOf(
                    KildematerialeKode.Selvrapportert,
                    KildematerialeKode.Salgsoppgave,
                    KildematerialeKode.Byggesaksdokumenter,
                    KildematerialeKode.AnnenDokumentasjon,
                    KildematerialeKode.Plantegninger,
                )

                else -> egenregistreringFelt.kildemateriale in listOf(
                    KildematerialeKode.Selvrapportert,
                    KildematerialeKode.Salgsoppgave,
                    KildematerialeKode.Byggesaksdokumenter,
                    KildematerialeKode.AnnenDokumentasjon,
                )
            }
        }
    }
}
