package no.kartverket.matrikkel.bygning.application.bygning

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import java.time.Instant

class BygningService(
    private val bygningClient: BygningClient,
    private val bygningRepository: BygningRepository,
) {
    fun getBygningByBubbleId(
        bygningBubbleId: Long,
        registreringstidspunkt: Instant = Instant.now(),
    ): Result<Bygning, DomainError> =
        bygningClient.getBygningByBubbleId(bygningBubbleId).map { bygning ->
            bygning.copy(
                bruksenheter =
                    bygning.bruksenheter.map { bruksenhet ->
                        bygningRepository.getBruksenhetById(bruksenhet.id.value, registreringstidspunkt) ?: bruksenhet
                    },
            )
        }

    fun getBruksenhetByBubbleId(
        bruksenhetBubbleId: Long,
        registreringstidspunkt: Instant = Instant.now(),
    ): Result<Bruksenhet, DomainError> =
        bygningClient
            .getBruksenhetByBubbleId(bruksenhetBubbleId)
            .map { bruksenhet ->
                bygningRepository.getBruksenhetById(bruksenhet.id.value, registreringstidspunkt) ?: bruksenhet
            }
}
