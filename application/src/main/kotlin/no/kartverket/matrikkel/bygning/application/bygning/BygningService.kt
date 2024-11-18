package no.kartverket.matrikkel.bygning.application.bygning

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import com.github.michaelbull.result.toResultOr
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.error.BruksenhetNotFound
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.withEgenregistrertData

class BygningService(
    private val bygningClient: BygningClient,
    private val egenregistreringService: EgenregistreringService
) {
    fun getBygningWithEgenregistrertData(bygningId: Long): Result<Bygning, DomainError> {
        return bygningClient
            .getBygningById(bygningId)
            .map { bygning ->
                val egenregistreringerForBygning = egenregistreringService.findAllEgenregistreringerForBygning(bygningId)
                bygning.withEgenregistrertData(egenregistreringerForBygning)
            }
    }

    fun getBruksenhetWithEgenregistrertData(bygningId: Long, bruksenhetId: Long): Result<Bruksenhet, DomainError> {
        return bygningClient
            .getBygningById(bygningId)
            .andThen { getBruksenhetWithEgenregistrertData(it, bruksenhetId) }
    }

    private fun getBruksenhetWithEgenregistrertData(
        bygning: Bygning,
        bruksenhetId: Long
    ): Result<Bruksenhet, DomainError> {
        val egenregistreringerForBygning = egenregistreringService.findAllEgenregistreringerForBygning(bygning.bygningId)
        return bygning.bruksenheter
            .find { it.bruksenhetId == bruksenhetId }
            ?.withEgenregistrertData(egenregistreringerForBygning)
            .toResultOr {
                BruksenhetNotFound(message = "Bruksenhet finnes ikke på bygningen")
            }
    }
}
