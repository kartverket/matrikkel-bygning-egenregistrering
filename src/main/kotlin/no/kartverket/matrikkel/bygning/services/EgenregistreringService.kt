package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.models.Result
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.repositories.EgenregistreringRepository

class EgenregistreringService(private val bygningClient: BygningClient, private val egenregistreringRepository: EgenregistreringRepository) {
    private val bygningRegistreringer: MutableList<BygningRegistrering> = mutableListOf()
    private val bruksenhetRegistreringer: MutableList<BruksenhetRegistrering> = mutableListOf()

    fun addEgenregistrering(egenregistrering: Egenregistrering): Result<Unit> {

        val bygning = bygningClient.getBygningById(egenregistrering.bygningId) ?: return Result.ErrorResult(
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

        val hvaSkalViHaHer = egenregistreringRepository.saveEgenregistrering(egenregistrering);

        return if (hvaSkalViHaHer) {
            Result.Success(Unit)
        } else {
            Result.ErrorResult(
                ErrorDetail(
                    detail = "noe greier"
                )
            )
        }
    }

    private fun isAllBruksenheterRegisteredOnCorrectBygning(
        egenregistrering: Egenregistrering, bygning: Bygning
    ): Boolean {
        if (egenregistrering.bruksenhetRegistreringer?.isEmpty() == true) return true

        return egenregistrering.bruksenhetRegistreringer?.any { bruksenhetRegistering ->
            bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId } != null
        } ?: true
    }
}
