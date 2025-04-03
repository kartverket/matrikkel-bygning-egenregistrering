package no.kartverket.matrikkel.bygning.application.bygning

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.error.ValidationError
import no.kartverket.matrikkel.bygning.application.models.ids.MatrikkelenhetBubbleId

class RegistrertEierService(
    private val registrertEierClient: RegistrertEierClient,
) {
    fun erUltimatEier(
        matrikkelenhetBubbleId: MatrikkelenhetBubbleId,
        eier: RegistreringAktoer.Foedselsnummer,
    ): Result<Unit, DomainError> =
        registrertEierClient
            .finnEierforhold(matrikkelenhetBubbleId, eier)
            .andThen { eierforhold ->
                if (eierforhold.ultimatEier) {
                    Ok(Unit)
                } else {
                    Err(ValidationError("Eier ${eier.value} er ikke ultimat eier av matrikkelenhet ${matrikkelenhetBubbleId.value}"))
                }
            }
}
