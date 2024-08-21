package no.kartverket.matrikkel.bygning.models

import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class ErrorResult(val errors: List<ErrorDetail>) : Result<Nothing>() {
        constructor(error: ErrorDetail) : this(listOf(error))
    }
}
