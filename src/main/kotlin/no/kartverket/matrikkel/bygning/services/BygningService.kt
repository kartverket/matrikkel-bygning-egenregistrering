package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Result
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail

class BygningService(
    private val bygningClient: BygningClient,
    private val egenregistreringService: EgenregistreringService,
) {
    fun getBygningWithEgenregistrertData(bygningId: Long): Result<Bygning> {
        val bygning = bygningClient.getBygningById(bygningId) ?: return Result.ErrorResult(
            ErrorDetail(
                detail = "Bygningen finnes ikke i matrikkelen",
            ),
        )

        val egenregistreringerForBygning = egenregistreringService.findAllEgenregistreringerForBygning(bygningId)

        val bygningWithEgenregistrertData = bygning.withEgenregistrertData(egenregistreringerForBygning)

        val aggregatedBygningWithAggregatedBruksenheter = bygningWithEgenregistrertData.withBruksenheter(
            bygningWithEgenregistrertData.bruksenheter.map { it.withEgenregistrertData(egenregistreringerForBygning) },
        )

        return Result.Success(aggregatedBygningWithAggregatedBruksenheter)

    }

    fun getBruksenhetWithEgenregistrertData(bygningId: Long, bruksenhetId: Long): Result<Bruksenhet> {
        val bygning = bygningClient.getBygningById(bygningId) ?: return Result.ErrorResult(
            ErrorDetail(
                detail = "Bygningen finnes ikke i matrikkelen",
            ),
        )

        val bruksenhet = bygning.bruksenheter.find { it.bruksenhetId == bruksenhetId }
//            ?.let { addEgenregistrerteDataForBruksenhet(it) }
            ?: return Result.ErrorResult(
                ErrorDetail(
                    detail = "Bruksenhet finnes ikke på bygningen",
                ),
            )

        return Result.Success(bruksenhet)
    }
}

