package no.kartverket.matrikkel.bygning.application.bygning

import com.github.michaelbull.result.Result
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.error.ErrorDetail

interface BygningClient {
    fun getBygningById(id: Long): Result<Bygning, ErrorDetail>

    fun getBygningByBygningsnummer(bygningsnummer: Long): Result<Bygning, ErrorDetail>
}
