package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Result
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.repositories.EgenregistreringRepository

class BygningService(
    private val bygningClient: BygningClient,
    private val egenregistreringRepository: EgenregistreringRepository,
) {
    fun getBygningWithEgenregistrertData(bygningId: Long): Result<Bygning> {
        val bygning = bygningClient.getBygningById(bygningId)
            ?: return Result.ErrorResult(
                ErrorDetail(
                    detail = "Bygningen finnes ikke i matrikkelen",
                ),
            )

        // Burde disse heller komme fra egenregistreringService, i stedet for å sende inn egenregistreringRepository her?
        val bygningWithEgenregistrertData = egenregistreringRepository.findNewestBygningRegistrering(bygningId)
            ?.let { bygning.withEgenregistrertData(it) }
            ?: bygning

        val bruksenheterWithEgenregistrertData = bygning.bruksenheter
            .map(::addEgenregistrerteDataForBruksenhet)

        return Result.Success(bygningWithEgenregistrertData.withBruksenheter(bruksenheterWithEgenregistrertData))

    }

    fun getBruksenhetWithEgenregistrertData(bygningId: Long, bruksenhetId: Long): Result<Bruksenhet> {
        val bygning = bygningClient.getBygningById(bygningId)
            ?: return Result.ErrorResult(
                ErrorDetail(
                    detail = "Bygningen finnes ikke i matrikkelen",
                ),
            )

        val bruksenhet = bygning.bruksenheter
            .find { it.bruksenhetId == bruksenhetId }
            ?.let { addEgenregistrerteDataForBruksenhet(it) }
            ?: return Result.ErrorResult(
                ErrorDetail(
                    detail = "Bruksenhet finnes ikke på bygningen",
                ),
            )

        return Result.Success(bruksenhet)
    }

    private fun addEgenregistrerteDataForBruksenhet(bruksenhet: Bruksenhet): Bruksenhet {
        return egenregistreringRepository.findNewestBruksenhetRegistrering(bruksenhet.bruksenhetId)
            ?.let { bruksenhet.withEgenregistrertData(it) }
            ?: bruksenhet
    }
}

