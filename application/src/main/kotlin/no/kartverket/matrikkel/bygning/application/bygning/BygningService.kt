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
                    // TODO Batchoperasjon
                    bygningRepository.getBruksenhetById(bruksenhet.id) ?: bruksenhet
                },
            )
        }
    }

    fun createBruksenhetSnapshotsOfEgenregistrering(bygning: Bygning, egenregistrering: Egenregistrering) {
        val bruksenheterRegistrert = egenregistrering.bygningRegistrering.bruksenhetRegistreringer.mapNotNull { bruksenhetRegistrering ->
            val bruksenhetInBygning =
                bygning.bruksenheter.find { bruksenhet -> bruksenhet.bruksenhetId == bruksenhetRegistrering.bruksenhetId }

            bruksenhetInBygning?.withEgenregistrertData(listOf(egenregistrering))
        }

        bruksenheterRegistrert.forEach { bruksenhet ->
            // TODO Nå lagrer man én og én bruksenhet i databasen, kan det være hensiktsmessig å lagre alle samtidig?
            bygningRepository.saveBruksenhet(bruksenhet)
        }
    }
}
