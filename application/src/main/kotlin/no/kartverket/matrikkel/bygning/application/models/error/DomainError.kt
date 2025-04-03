package no.kartverket.matrikkel.bygning.application.models.error

// Alle feil som kan skje i domenet
sealed interface DomainError

data class BygningNotFound(
    val message: String,
) : DomainError

data class BruksenhetNotFound(
    val message: String,
) : DomainError

data class MatrikkelenhetNotFound(
    val message: String,
) : DomainError

data class ValidationError(
    val message: String,
) : DomainError

data class MultipleValidationError(
    val errors: List<ValidationError>,
) : DomainError
