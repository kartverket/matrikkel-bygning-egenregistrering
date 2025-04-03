package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.toResultOr
import no.kartverket.matrikkel.bygning.application.bygning.RegistrertEierClient
import no.kartverket.matrikkel.bygning.application.models.MatrikkelenhetEier
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.error.MatrikkelenhetNotFound
import no.kartverket.matrikkel.bygning.application.models.ids.MatrikkelenhetBubbleId

class LocalRegistrertEierClient : RegistrertEierClient {
    private val matrikkelenhetEiere: List<MatrikkelenhetEier> =
        listOf(
            MatrikkelenhetEier(
                ultimatEier = true,
                matrikkelenhetBubbleId = MatrikkelenhetBubbleId(1L),
                eier = RegistreringAktoer.Foedselsnummer("66860475309"),
            ),
        )

    override fun finnEierforhold(
        matrikkelenhetBubbleId: MatrikkelenhetBubbleId,
        eier: RegistreringAktoer.Foedselsnummer,
    ): Result<MatrikkelenhetEier, DomainError> =
        matrikkelenhetEiere
            .find { it.matrikkelenhetBubbleId == matrikkelenhetBubbleId && it.eier == eier }
            .toResultOr {
                MatrikkelenhetNotFound(
                    message = "Fant ikke eierforhold mellom matrikkelenhet ${matrikkelenhetBubbleId.value} og eier ${eier.value}",
                )
            }
}
