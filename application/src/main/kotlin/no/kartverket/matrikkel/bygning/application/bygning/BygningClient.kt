package no.kartverket.matrikkel.bygning.application.bygning

import com.github.michaelbull.result.Result
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.error.DomainError

interface BygningClient {
    fun getBygningById(id: Long): Result<Bygning, DomainError>

    fun getBygningByBygningsnummer(bygningsnummer: Long): Result<Bygning, DomainError>
}
