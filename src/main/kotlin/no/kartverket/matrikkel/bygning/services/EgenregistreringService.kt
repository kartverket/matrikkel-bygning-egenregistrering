package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.matrikkel.Bygning
import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.Result
import no.kartverket.matrikkel.bygning.models.requests.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail

class EgenregistreringService(private val bygningClient: BygningClient) {
    private val bygningRegistreringer: MutableList<BygningRegistrering> = mutableListOf()
    private val bruksenhetRegistreringer: MutableList<BruksenhetRegistrering> = mutableListOf()

    fun addEgenregistreringToBygning(bygningId: Long, egenregistrering: EgenregistreringRequest): Result<Unit> {
        val bygning = bygningClient.getBygningById(bygningId)
            ?: return Result.ErrorResult(
                ErrorDetail(
                    detail = "Bygningen finnes ikke i matrikkelen",
                ),
            )

        if (!isAllBruksenheterRegisteredOnCorrectBygning(egenregistrering, bygning)) {
            return Result.ErrorResult(
                ErrorDetail(
                    detail = "Bruksenheten finnes ikke i bygningen",
                ),
            )
        }

        addEgenregistreringToBygning(egenregistrering)
        addEgenregistreringToBruksenhet(egenregistrering)
        return Result.Success(Unit)
    }

    private fun isAllBruksenheterRegisteredOnCorrectBygning(
        egenregistrering: EgenregistreringRequest,
        bygning: Bygning
    ): Boolean {
        if (egenregistrering.bruksenhetRegistreringer?.isEmpty() == true) return true

        return egenregistrering.bruksenhetRegistreringer?.any { bruksenhetRegistering ->
            bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId } != null
        } ?: true
    }

    private fun addEgenregistreringToBygning(egenregistrering: EgenregistreringRequest) {
        bygningRegistreringer.add(
            BygningRegistrering(
                bruksarealRegistrering = egenregistrering.bygningRegistrering.bruksarealRegistrering,
                byggeaarRegistrering = egenregistrering.bygningRegistrering.byggeaarRegistrering,
                vannforsyningRegistrering = egenregistrering.bygningRegistrering.vannforsyningRegistrering,
                avlopRegistrering = egenregistrering.bygningRegistrering.avlopRegistrering,
            ),
        )
    }

    private fun addEgenregistreringToBruksenhet(egenregistrering: EgenregistreringRequest) {
        egenregistrering.bruksenhetRegistreringer?.forEach { bruksenhetRegistrering ->
            bruksenhetRegistreringer.add(
                BruksenhetRegistrering(
                    bruksenhetId = bruksenhetRegistrering.bruksenhetId,
                    bruksarealRegistrering = bruksenhetRegistrering.bruksarealRegistrering,
                    energikildeRegistrering = bruksenhetRegistrering.energikildeRegistrering,
                    oppvarmingRegistrering = bruksenhetRegistrering.oppvarmingRegistrering,
                ),
            )
        }
    }
}
