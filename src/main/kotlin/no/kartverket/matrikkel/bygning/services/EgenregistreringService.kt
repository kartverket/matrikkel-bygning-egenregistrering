package no.kartverket.matrikkel.bygning.services

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.models.Result
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.repositories.EgenregistreringRepository

class EgenregistreringService(
    private val bygningClient: BygningClient, private val egenregistreringRepository: EgenregistreringRepository
) {
    fun addEgenregistrering(egenregistrering: Egenregistrering): Result<Unit> {

        val bygning = bygningClient.getBygningById(egenregistrering.bygningId) ?: return Result.ErrorResult(
            ErrorDetail(
                detail = "Bygning med ID ${egenregistrering.bygningId} finnes ikke i matrikkelen",
            ),
        )

        val invalidBruksenheter = findBruksenheterNotRegisteredOnCorrectBygning(egenregistrering, bygning)
        if (invalidBruksenheter.isNotEmpty()) {
            return Result.ErrorResult(
                ErrorDetail(
                    detail = "Bruksenhet${if (invalidBruksenheter.size > 1) "er" else ""} med ID ${invalidBruksenheter.joinToString()} finnes ikke i bygning med ID ${bygning.bygningId}",
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
                    detail = "Noe gikk galt under lagring av egenregistrering",
                ),
            )
        }
    }

    private fun findBruksenheterNotRegisteredOnCorrectBygning(
        egenregistrering: Egenregistrering, bygning: Bygning
    ): List<Long> {
        if (egenregistrering.bruksenhetRegistreringer?.isEmpty() == true) return emptyList()

        return egenregistrering.bruksenhetRegistreringer?.mapNotNull { bruksenhetRegistering ->
            val bruksenhet = bygning.bruksenheter.find { it.bruksenhetId == bruksenhetRegistering.bruksenhetId }

            if (bruksenhet == null) {
                return@mapNotNull bruksenhetRegistering.bruksenhetId
            } else {
                return@mapNotNull null
            }
        } ?: emptyList()
    }
}
