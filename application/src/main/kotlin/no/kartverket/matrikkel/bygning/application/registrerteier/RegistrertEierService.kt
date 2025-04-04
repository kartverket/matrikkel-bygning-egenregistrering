package no.kartverket.matrikkel.bygning.application.registrerteier

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.error.IkkeUltimatEier
import no.kartverket.matrikkel.bygning.application.models.ids.MatrikkelenhetBubbleId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RegistrertEierService(
    private val registrertEierClient: RegistrertEierClient,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

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
                    // TODO: fjern fnr fra logglinje
                    log.warn(
                        "Eier ${eier.value} er ikke ultimat eier av matrikkelenhet ${matrikkelenhetBubbleId.value}",
                    )
                    Err(IkkeUltimatEier("Eier kan ikke registrere på bruksenheten"))
                }
            }
}
