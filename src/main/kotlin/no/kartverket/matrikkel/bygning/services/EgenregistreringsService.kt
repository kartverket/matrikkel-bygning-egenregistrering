package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.matrikkel.Bygning
import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.Result
import no.kartverket.matrikkel.bygning.models.requests.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BygningsRegistrering
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail

class EgenregistreringsService(private val bygningClient: BygningClient) {
    private val bygningRegistreringer: MutableList<BygningsRegistrering> = mutableListOf()
    private val bruksenhetRegistreringer: MutableList<BruksenhetRegistrering> = mutableListOf()

    fun addEgenregistreringToBygning(bygningId: Long, egenregistrering: EgenregistreringRequest): Result<Unit> {
        val validationErrors = EgenregistreringValidationService.validateEgenregistreringRequest(egenregistrering)

        if (validationErrors.isNotEmpty()) {
            return Result.ErrorResult(validationErrors)
        }

        val bygning = bygningClient.getBygningById(bygningId)
            ?: return Result.ErrorResult(
                ErrorDetail(
                    detail = "Bygningen finnes ikke i matrikkelen",
                ),
            )

        return when (alleBruksenheterErRegistrertPaaKorrektBygning(egenregistrering, bygning)) {
            true -> {
                addEgenregistreringToBygning(egenregistrering)
                addEgenregistreringToBruksenhet(egenregistrering)
                Result.Success(Unit)
            }
            false -> {
                Result.ErrorResult(
                    ErrorDetail(
                        detail = "Bruksenheten finnes ikke i bygningen",
                    ),
                )
            }
        }
    }

    private fun alleBruksenheterErRegistrertPaaKorrektBygning(
        egenregistrering: EgenregistreringRequest,
        bygning: Bygning
    ) = egenregistrering.bruksenhetRegistreringer.any { bruksenhetRegistering ->
        bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId } != null
    }

    private fun addEgenregistreringToBygning(egenregistrering: EgenregistreringRequest) {
        bygningRegistreringer.add(
            BygningsRegistrering(
                bruksareal = egenregistrering.bygningsRegistrering.bruksareal,
                byggeaar = egenregistrering.bygningsRegistrering.byggeaar,
                vannforsyning = egenregistrering.bygningsRegistrering.vannforsyning,
                avlop = egenregistrering.bygningsRegistrering.avlop,
            ),
        )
    }

    private fun addEgenregistreringToBruksenhet(egenregistrering: EgenregistreringRequest) {
        egenregistrering.bruksenhetRegistreringer.forEach { bruksenhetRegistrering ->
            bruksenhetRegistreringer.add(
                BruksenhetRegistrering(
                    bruksenhetId = bruksenhetRegistrering.bruksenhetId,
                    bruksareal = bruksenhetRegistrering.bruksareal,
                    energikilde = bruksenhetRegistrering.energikilde,
                    oppvarming = bruksenhetRegistrering.oppvarming,
                ),
            )
        }
    }
}
