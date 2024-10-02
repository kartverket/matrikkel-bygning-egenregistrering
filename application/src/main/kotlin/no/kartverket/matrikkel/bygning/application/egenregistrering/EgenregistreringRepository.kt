package no.kartverket.matrikkel.bygning.application.egenregistrering

import com.github.michaelbull.result.Result
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.error.ErrorDetail

interface EgenregistreringRepository {
    fun getAllEgenregistreringerForBygning(bygningId: Long): Result<List<Egenregistrering>, ErrorDetail>
    fun saveEgenregistrering(egenregistrering: Egenregistrering): Result<Unit, ErrorDetail>
}
