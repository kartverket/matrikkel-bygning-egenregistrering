package no.kartverket.matrikkel.bygning.matrikkel

import com.github.michaelbull.result.Result
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail

interface BygningClient {
    fun getBygningById(id: Long): Result<Bygning, ErrorDetail>

    fun getBygningByBygningsnummer(bygningsnummer: Long): Result<Bygning, ErrorDetail>
}
