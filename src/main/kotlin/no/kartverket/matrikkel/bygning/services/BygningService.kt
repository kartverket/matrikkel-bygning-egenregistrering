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
        val bygning = bygningClient.getBygningById(bygningId)
            ?: return Result.ErrorResult(
                ErrorDetail(
                    detail = "Bygningen finnes ikke i matrikkelen",
                ),
            )

        val newestEgenregistrering = egenregistreringService.findNewestEgenregistreringForBygning(bygningId)

        val bygningWithEgenregistrertData = if (newestEgenregistrering != null) {
            bygning.withEgenregistrertData(newestEgenregistrering.registreringstidspunkt, newestEgenregistrering.bygningRegistrering)
        } else bygning

        val bruksenheterWithEgenregistrertData = bygning.bruksenheter.map { bruksenhet ->
            val bruksenhetRegistrering =
                newestEgenregistrering?.bygningRegistrering?.bruksenhetRegistreringer?.find { it.bruksenhetId == bruksenhet.bruksenhetId }
            if (bruksenhetRegistrering != null) {
                bruksenhet.withEgenregistrertData(newestEgenregistrering.registreringstidspunkt, bruksenhetRegistrering)
            } else bruksenhet
        }


        val egenregisteringerForBygning = egenregistreringService.findAllEgenregistreringerForBygning(bygningId)

        val aggregatedBygningRegistrering =


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
//            ?.let { addEgenregistrerteDataForBruksenhet(it) }
            ?: return Result.ErrorResult(
                ErrorDetail(
                    detail = "Bruksenhet finnes ikke på bygningen",
                ),
            )

        return Result.Success(bruksenhet)
    }
}

