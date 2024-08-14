package no.kartverket.matrikkel.bygning.models.responses

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorDetail(
    val pointer: String? = null,
    val detail: String,
)

@Serializable
sealed class ErrorResponse(
    val status: Int,
    val title: String,
    val detail: String,
) {
    // Disse kan ikke settes som vanlige properties, på grunn av en pågående bug i forbindelse med @Serializable annotasjonen, se:
    // https://youtrack.jetbrains.com/issue/KT-38958 og
    // https://stackoverflow.com/questions/63173082/how-to-serialize-kotlin-sealed-class-with-open-val-using-kotlinx-serialization/63539183#63539183
    abstract val correlationId: String?
    abstract val errorDetails: List<ErrorDetail>

    @Serializable
    class ValidationError(
        override val correlationId: String?, override val errorDetails: List<ErrorDetail>
    ) : ErrorResponse(
        status = HttpStatusCode.BadRequest.value,
        title = "Valideringsfeil",
        detail = "Requesten din inneholdt én eller flere felter som ikke kunne valideres. Se listen over feil for flere detaljer",
    )

    @Serializable
    class BadRequestError(
        override val correlationId: String?, override val errorDetails: List<ErrorDetail>
    ) : ErrorResponse(
        status = HttpStatusCode.BadRequest.value,
        title = "Formateringsfeil",
        detail = "Requesten din inneholdt én eller flere felter som ikke var formatert riktig. Se listen over feil for flere detaljer",
    )

    @Serializable
    class InternalServerError(
        override val correlationId: String?, override val errorDetails: List<ErrorDetail>
    ) : ErrorResponse(
        status = HttpStatusCode.InternalServerError.value,
        title = "Serverfeil",
        detail = "Noe har gått galt på serveren. Ta kontakt med Kartverket hvis feilen vedvarer",
    )
}
