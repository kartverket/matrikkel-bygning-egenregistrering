package no.kartverket.matrikkel.bygning.routes.v1.common

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.error.BruksenhetNotFound
import no.kartverket.matrikkel.bygning.application.models.error.BygningNotFound
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.error.MultipleValidationError
import no.kartverket.matrikkel.bygning.application.models.error.ValidationError
import org.slf4j.MDC

// Gjør at man kan chaine direkte videre.
// Ulempen er at vi mapper og vet om "domene"-feil ytterst i web-laget
// Kan kanskje være hensiktsmessig å ha custom exception-typer
fun exceptionToDomainError(e: Throwable): DomainError =
    when (e) {
        is IllegalArgumentException -> ValidationError(e.message ?: "Ugyldig request")
        else -> ValidationError(e.message ?: "Ugyldig request")
    }

fun domainErrorToResponse(error: DomainError): Pair<HttpStatusCode, ErrorResponse> =
    when (error) {
        is BygningNotFound -> HttpStatusCode.NotFound to ErrorResponse.NotFoundError(description = error.message)
        is BruksenhetNotFound -> HttpStatusCode.NotFound to ErrorResponse.NotFoundError(description = error.message)
        is ValidationError -> HttpStatusCode.BadRequest to ErrorResponse.BadRequestError(description = error.message)
        is MultipleValidationError ->
            HttpStatusCode.BadRequest to
                ErrorResponse.ValidationError(
                    details =
                        error.errors.map {
                            it.message
                        },
                )
    }

sealed interface ErrorResponse {
    val status: Int
    val title: String
    val description: String
    val correlationId: String
    val details: List<String>

    @Serializable
    class ValidationError(
        override val status: Int = HttpStatusCode.BadRequest.value,
        override val title: String = "Valideringsfeil",
        override val description: String =
            "Requesten din inneholdt én eller flere felter som ikke kunne valideres." +
                "Se listen over feil for flere detaljer",
        override val correlationId: String = resolveCallID(),
        override val details: List<String> = emptyList(),
    ) : ErrorResponse {
        constructor(vararg details: String) : this(details = details.toList())
    }

    @Serializable
    class BadRequestError(
        override val status: Int = HttpStatusCode.BadRequest.value,
        override val title: String = "Formateringsfeil",
        override val description: String =
            "Requesten din inneholdt én eller flere felter som ikke var formatert riktig." +
                "Se listen over feil for flere detaljer",
        override val correlationId: String = resolveCallID(),
        override val details: List<String> = emptyList(),
    ) : ErrorResponse {
        constructor(vararg details: String) : this(details = details.toList())
    }

    @Serializable
    class InternalServerError(
        override val status: Int = HttpStatusCode.InternalServerError.value,
        override val title: String = "Serverfeil",
        override val description: String = "Noe har gått galt på serveren. Ta kontakt med Kartverket hvis feilen vedvarer",
        override val correlationId: String = resolveCallID(),
        override val details: List<String> = emptyList(),
    ) : ErrorResponse {
        constructor(vararg details: String) : this(details = details.toList())
    }

    @Serializable
    class NotFoundError(
        override val status: Int = HttpStatusCode.NotFound.value,
        override val title: String = "Ikke funnet",
        override val description: String = "Ressursen du etterspurte kunne ikke bli funnet",
        override val correlationId: String = resolveCallID(),
        override val details: List<String> = emptyList(),
    ) : ErrorResponse {
        constructor(vararg details: String) : this(details = details.toList())
    }
}

fun resolveCallID(): String = MDC.get("request_id") ?: ""
