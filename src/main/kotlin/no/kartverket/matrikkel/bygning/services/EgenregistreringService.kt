package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.models.Result
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.repositories.EgenregistreringRepository

class EgenregistreringService(private val bygningClient: BygningClient, private val egenregistreringRepository: EgenregistreringRepository) {
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

        val egenregistreringWasSaved = egenregistreringRepository.saveEgenregistrering(egenregistrering);

        return if (egenregistreringWasSaved) {
            Result.Success(Unit)
        } else {
            // TODO Dette er egentlig en 500 error, for noe har gått galt men vi vet ikke hva.
            // Skal vi ha en måte å sende ut "internal server error" fra services?
            Result.ErrorResult(
                ErrorDetail(
                    detail = "Noe gikk galt under lagring av egenregistrering"
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
