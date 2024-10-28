package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import no.kartverket.matrikkel.bygning.application.bygning.BygningClient
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.ErrorDetail

class EgenregistreringService(
    private val bygningClient: BygningClient,
    private val egenregistreringRepository: EgenregistreringRepository,
) {
    // TODO ErrorDetail er ikke godt nok for å faktisk plukke opp flere Errors, så her dukker det opp litt problem
    // Hvordan skal vi håndtere potensielle flere feil? Trenger vi egne feiltyper overalt?
    fun addEgenregistrering(egenregistrering: Egenregistrering): Result<Unit, ErrorDetail> {
        return bygningClient
            .getBygningById(egenregistrering.bygningRegistrering.bygningId)
            .andThen { bygning ->
                val validationErrors = EgenregistreringValidator.validateEgenregistrering(egenregistrering, bygning)

                if (validationErrors.isEmpty()) {
                    Ok(bygning)
                } else {
                    Err(validationErrors.first())
                }
            }
            .andThen { egenregistreringRepository.saveEgenregistrering(egenregistrering) }
    }

    fun findAllEgenregistreringerForBygning(bygningId: Long): Result<List<Egenregistrering>, ErrorDetail> {
        return egenregistreringRepository.getAllEgenregistreringerForBygning(bygningId)
    }
}
