package no.kartverket.matrikkel.bygning.application.egenregistrering

import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.ErrorDetail


class EgenregistreringValidator {
    companion object {
        // Begge disse validatorene kunne ha lagt ved indeks på bruksenheten og gitt dette i pointer, men blir det litt overkill?
        fun validateEgenregistrering(egenregistrering: Egenregistrering, bygning: Bygning): List<ErrorDetail> {
            return listOfNotNull(
                validateBruksenheterRegistreredOnCorrectBygning(egenregistrering, bygning),
                validateRepeatedBruksenheter(egenregistrering),
            )
        }

        private fun validateBruksenheterRegistreredOnCorrectBygning(
            egenregistrering: Egenregistrering, bygning: Bygning
        ): ErrorDetail? {
            val invalidBruksenheter = egenregistrering.bygningRegistrering.bruksenhetRegistreringer
                .mapNotNull { bruksenhetRegistering ->
                    val bruksenhet = bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId }

                    if (bruksenhet == null) {
                        bruksenhetRegistering.bruksenhetId
                    } else {
                        null
                    }
                }

            if (invalidBruksenheter.isNotEmpty()) {
                return ErrorDetail(
                    detail = "Bruksenhet${if (invalidBruksenheter.size > 1) "er" else ""} med ID ${invalidBruksenheter.joinToString()} finnes ikke i bygning med ID ${bygning.bygningId}",
                )
            }

            return null
        }

        private fun validateRepeatedBruksenheter(egenregistrering: Egenregistrering): ErrorDetail? {
            val repeatedBruksenheter = egenregistrering.bygningRegistrering.bruksenhetRegistreringer
                .groupBy { it.bruksenhetId }
                .filter { it.value.size > 1 }
                .map { it.key }


            if (repeatedBruksenheter.isNotEmpty()) {
                return ErrorDetail(
                    detail = "Bruksenhet ${if (repeatedBruksenheter.size > 1) "er" else ""} med ID ${repeatedBruksenheter.joinToString()} har flere registreringer på seg. Kun én registrering per bruksenhet kan sendes inn",
                )
            }

            return null
        }
    }
}
