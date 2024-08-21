package no.kartverket.matrikkel.bygning.models.responses

import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.slf4j.MDC

@Serializable
data class ErrorDetail(
    val pointer: String? = null,
    val detail: String,
)

sealed interface ErrorResponse {
    val status: Int
    val title: String
    val description: String
    val correlationId: String
    val details: List<ErrorDetail>

    @Serializable
    class ValidationError(
        override val status: Int = HttpStatusCode.BadRequest.value,
        override val title: String = "Valideringsfeil",
        override val description: String = "Requesten din inneholdt én eller flere felter som ikke kunne valideres. Se listen over feil for flere detaljer",
        override val correlationId: String = resolveCallID(),
        override val details: List<ErrorDetail> = emptyList(),
    ) : ErrorResponse

    @Serializable
    class BadRequestError(
        override val status: Int = HttpStatusCode.BadRequest.value,
        override val title: String = "Formateringsfeil",
        override val description: String = "Requesten din inneholdt én eller flere felter som ikke var formatert riktig. Se listen over feil for flere detaljer",
        override val correlationId: String = resolveCallID(),
        override val details: List<ErrorDetail> = emptyList(),
    ) : ErrorResponse


    @Serializable
    class InternalServerError(
        override val status: Int = HttpStatusCode.InternalServerError.value,
        override val title: String = "Serverfeil",
        override val description: String = "Noe har gått galt på serveren. Ta kontakt med Kartverket hvis feilen vedvarer",
        override val correlationId: String = resolveCallID(),
        override val details: List<ErrorDetail> = emptyList(),
    ) : ErrorResponse
}

fun resolveCallID(): String {
    return MDC.get("call-id") ?: ""
}
