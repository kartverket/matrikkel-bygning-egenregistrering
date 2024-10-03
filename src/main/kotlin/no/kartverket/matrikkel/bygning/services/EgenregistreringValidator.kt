package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail

class EgenregistreringValidator() {
    companion object {
        fun validateEgenregistrering(egenregistrering: Egenregistrering): List<ErrorDetail> {
            val isValidEierFoedselsnummer = egenregistrering.eier.validate()

            if (!isValidEierFoedselsnummer) return listOf(
                ErrorDetail(
                    pointer = "eier",
                    detail = "Fødeslsnummer for eier er ikke et gyldig fødselsnummer",
                ),
            )

            return emptyList()
        }
    }
}
