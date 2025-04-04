package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.toResultOr
import no.kartverket.matrikkel.bygning.application.models.MatrikkelenhetEier
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.error.FantIkkeEierForhold
import no.kartverket.matrikkel.bygning.application.models.ids.MatrikkelenhetBubbleId
import no.kartverket.matrikkel.bygning.application.registrerteier.RegistrertEierClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LocalRegistrertEierClient : RegistrertEierClient {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private val matrikkelenhetEiere: List<MatrikkelenhetEier> =
        listOf(
            MatrikkelenhetEier(
                ultimatEier = true,
                matrikkelenhetBubbleId = MatrikkelenhetBubbleId(1001),
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
                // TODO: fjern fnr fra logglinje
                log.warn(
                    "Fant ikke eierforhold mellom matrikkelenhet ${matrikkelenhetBubbleId.value} og eier ${eier.value}",
                )
                FantIkkeEierForhold(
                    message = "Fant ikke eierforhold",
                )
            }
}
