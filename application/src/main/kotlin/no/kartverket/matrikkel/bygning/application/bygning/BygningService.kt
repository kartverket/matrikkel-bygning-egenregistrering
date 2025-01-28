package no.kartverket.matrikkel.bygning.application.bygning

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.withEgenregistrertData

class BygningService(
    private val bygningClient: BygningClient,
    private val bygningRepository: BygningRepository,
) {
    fun getBygningById(bygningId: Long): Result<Bygning, DomainError> {
        return bygningClient.getBygningById(bygningId).map { bygning ->
            bygning.copy(
                bruksenheter = bygning.bruksenheter.map { bruksenhet ->
                    bygningRepository.getBruksenhetById(bruksenhet.id) ?: bruksenhet
                },
            )
        }
    }

    fun createBruksenhetSnapshotsOfEgenregistrering(bygning: Bygning, egenregistrering: Egenregistrering) {
        egenregistrering.bygningRegistrering.bruksenhetRegistreringer.forEach { bruksenhetRegistrering ->
            val bruksenhetInBygning =
                bygning.bruksenheter.find { bruksenhet -> bruksenhet.bruksenhetBubbleId == bruksenhetRegistrering.bruksenhetBubbleId }

            bruksenhetInBygning?.withEgenregistrertData(egenregistrering)?.let { bruksenhet ->
                bygningRepository.saveBruksenhet(bruksenhet)
            }
        }
    }
}
