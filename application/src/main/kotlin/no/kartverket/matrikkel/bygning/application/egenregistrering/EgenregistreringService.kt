package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Result
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.DomainError

interface EgenregistreringService {
    fun addEgenregistrering(egenregistrering: Egenregistrering): Result<Unit, DomainError>
}
