package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.Result
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.routes.v1.dto.request.EgenregistreringRequest
import java.time.Instant
import java.util.*

class EgenregistreringService(private val bygningClient: BygningClient) {
    private val bygningRegistreringer: MutableList<BygningRegistrering> = mutableListOf()
    private val bruksenhetRegistreringer: MutableList<BruksenhetRegistrering> = mutableListOf()

    fun addEgenregistreringToBygning(egenregistrering: EgenregistreringRequest): Result<Unit> {
        val bygningId = egenregistrering.bygningId

        val bygning = bygningClient.getBygningById(bygningId) ?: return Result.ErrorResult(
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

        saveEgenregistreringToBygning(egenregistrering)
        saveEgenregistreringToBruksenhet(egenregistrering)
        return Result.Success(Unit)
    }

    private fun isAllBruksenheterRegisteredOnCorrectBygning(
        egenregistrering: EgenregistreringRequest, bygning: Bygning
    ): Boolean {
        if (egenregistrering.bruksenhetRegistreringer?.isEmpty() == true) return true

        return egenregistrering.bruksenhetRegistreringer?.any { bruksenhetRegistering ->
            bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId } != null
        } ?: true
    }

    fun findNewestEgenregistreringForBygning(bygningId: Long): BygningRegistrering? {
        return bygningRegistreringer
            .filter { it.bygningId == bygningId }
            .maxByOrNull { it.registreringTidspunkt }
    }

    fun findNewestEgenregistreringForBruksenhet(bruksenhetId: Long): BruksenhetRegistrering? {
        return bruksenhetRegistreringer
            .filter { it.bruksenhetId == bruksenhetId }
            .maxByOrNull { it.registreringTidspunkt }
    }

    // Dummy while no persistence exists
    private fun saveEgenregistreringToBygning(egenregistrering: EgenregistreringRequest) {
        egenregistrering.bygningRegistrering?.let {
            bygningRegistreringer.add(
                BygningRegistrering(
                    registreringTidspunkt = Instant.now(),
                    registreringId = UUID.randomUUID(),
                    bruksarealRegistrering = egenregistrering.bygningRegistrering.bruksarealRegistrering,
                    byggeaarRegistrering = egenregistrering.bygningRegistrering.byggeaarRegistrering,
                    vannforsyningRegistrering = egenregistrering.bygningRegistrering.vannforsyningRegistrering,
                    avlopRegistrering = egenregistrering.bygningRegistrering.avlopRegistrering,
                    bygningId = egenregistrering.bygningId,
                ),
            )
        }
    }

    // Dummy while no persistence exists
    private fun saveEgenregistreringToBruksenhet(egenregistrering: EgenregistreringRequest) {
        egenregistrering.bruksenhetRegistreringer?.forEach { bruksenhetRegistrering ->
            bruksenhetRegistreringer.add(
                BruksenhetRegistrering(
                    registreringTidspunkt = Instant.now(),
                    registreringId = UUID.randomUUID(),
                    bruksenhetId = bruksenhetRegistrering.bruksenhetId,
                    bruksarealRegistrering = bruksenhetRegistrering.bruksarealRegistrering,
                    energikildeRegistrering = bruksenhetRegistrering.energikildeRegistrering,
                    oppvarmingRegistrering = bruksenhetRegistrering.oppvarmingRegistrering,
                ),
            )
        }
    }
}
