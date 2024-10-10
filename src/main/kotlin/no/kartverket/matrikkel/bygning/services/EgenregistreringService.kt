package no.kartverket.matrikkel.bygning.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.repositories.EgenregistreringRepository

class EgenregistreringService(
    private val bygningClient: BygningClient,
    private val egenregistreringRepository: EgenregistreringRepository,
) {
    fun addEgenregistrering(egenregistrering: Egenregistrering): Result<Unit, ErrorDetail> {
        return bygningClient
            .getBygningById(egenregistrering.bygningRegistrering.bygningId)
            .andThen { bygning ->
                val invalidBruksenheter = findBruksenheterNotRegisteredOnCorrectBygning(egenregistrering, bygning)

                if (invalidBruksenheter.isEmpty()) {
                    Ok(bygning)
                } else {
                    Err(
                        ErrorDetail(
                            detail = "Bruksenhet${if (invalidBruksenheter.size > 1) "er" else ""} med ID ${invalidBruksenheter.joinToString()} finnes ikke i bygning med ID $ { it.bygningId }",
                        ),
                    )
                }
            }
            .andThen { egenregistreringRepository.saveEgenregistrering(egenregistrering) }
    }

    private fun findBruksenheterNotRegisteredOnCorrectBygning(egenregistrering: Egenregistrering, bygning: Bygning): List<Long> {
        return egenregistrering.bygningRegistrering.bruksenhetRegistreringer.mapNotNull { bruksenhetRegistering ->
            val bruksenhet =
                bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId }

            if (bruksenhet == null) {
                bruksenhetRegistering.bruksenhetId
            } else {
                null
            }
        }
    }

    fun findAllEgenregistreringerForBygning(bygningId: Long): Result<List<Egenregistrering>, ErrorDetail> {
        return egenregistreringRepository.getAllEgenregistreringerForBygning(bygningId)
    }
}
