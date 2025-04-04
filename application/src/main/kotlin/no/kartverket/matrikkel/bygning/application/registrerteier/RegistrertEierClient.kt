package no.kartverket.matrikkel.bygning.application.registrerteier

import com.github.michaelbull.result.Result
import no.kartverket.matrikkel.bygning.application.models.MatrikkelenhetEier
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.ids.MatrikkelenhetBubbleId

interface RegistrertEierClient {
    fun finnEierforhold(
        matrikkelenhetBubbleId: MatrikkelenhetBubbleId,
        eier: RegistreringAktoer.Foedselsnummer,
    ): Result<MatrikkelenhetEier, DomainError>
}
