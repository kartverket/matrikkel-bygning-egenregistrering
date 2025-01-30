package no.kartverket.matrikkel.bygning.application.bygning

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import com.github.michaelbull.result.toResultOr
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.applyEgenregistreringer
import no.kartverket.matrikkel.bygning.application.models.error.BruksenhetNotFound
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import java.time.Instant

class BygningService(
    private val bygningClient: BygningClient,
    private val bygningRepository: BygningRepository,
) {
    fun getBygningByBubbleId(bygningBubbleId: Long, fremTilDato: Instant = Instant.now()): Result<Bygning, DomainError> {
        return bygningClient.getBygningByBubbleId(bygningBubbleId).map { bygning ->
            bygning.copy(
                bruksenheter = bygning.bruksenheter.map { bruksenhet ->
                    bygningRepository.getBruksenhetById(bruksenhet.id.value, fremTilDato) ?: bruksenhet
                },
            )
        }
    }

    fun getBruksenhetByBubbleId(bygningBubbleId: Long, bruksenhetBubbleId: Long, fremTilDato: Instant = Instant.now()): Result<Bruksenhet, DomainError> {
        return bygningClient.getBygningByBubbleId(bygningBubbleId)
            .andThen { bygning ->
                bygning.bruksenheter
                    .firstOrNull { bruksenhet -> bruksenhet.bruksenhetBubbleId.value == bruksenhetBubbleId }
                    .toResultOr {
                        BruksenhetNotFound("Fant ikke bruksenhet med id $bruksenhetBubbleId i bygning med id $bygningBubbleId")
                    }
            }
            .map { bruksenhet ->
                bygningRepository.getBruksenhetById(bruksenhet.id.value, fremTilDato) ?: bruksenhet
            }
    }

    fun createBruksenhetSnapshotsOfEgenregistrering(bygning: Bygning, egenregistrering: Egenregistrering) {
        egenregistrering.bygningRegistrering.bruksenhetRegistreringer.forEach { bruksenhetRegistrering ->
            val bruksenhetInBygning =
                bygning.bruksenheter.find { bruksenhet -> bruksenhet.bruksenhetBubbleId == bruksenhetRegistrering.bruksenhetBubbleId }

            bruksenhetInBygning?.applyEgenregistreringer(egenregistrering)?.let { bruksenhet ->
                bygningRepository.saveBruksenhet(bruksenhet)
            }
        }
    }
}
