package no.kartverket.matrikkel.bygning.application.models.error

data class ErrorDetail(
    val pointer: String? = null,
    val detail: String,
)
