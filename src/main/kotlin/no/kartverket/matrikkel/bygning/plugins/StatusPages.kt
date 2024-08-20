package no.kartverket.matrikkel.bygning.plugins

import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.models.responses.ErrorResponse

fun Application.configureStatusPages() {
    fun ApplicationCall.getCallId(): String? {
        return this.response.headers[HttpHeaders.XRequestId]
    }

    install(StatusPages) {
        exception<BadRequestException> { call, exception ->
            when (exception) {
                is MissingRequestParameterException -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse.InternalServerError(
                            call.getCallId(),
                            listOf(
                                ErrorDetail(
                                    detail = "Request URLen kunne ikke hente et parameter. Dette tyder på en utviklingsfeil",
                                ),
                            ),
                        ),
                    )
                }

                else -> {
                    when (val cause = exception.cause) {
                        is JsonConvertException -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse.BadRequestError(
                                    call.getCallId(),
                                    listOf(
                                        // TODO Prøve å hente ut informasjon om felt og sånt via feilmelding?
                                        ErrorDetail(
                                            detail = cause.message ?: "Ukjent feil etter serialisering av request objekt",
                                        ),
                                    ),
                                ),
                            )
                        }
                    }
                }
            }

            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse.BadRequestError(
                    call.getCallId(),
                    listOf(
                        ErrorDetail(
                            detail = exception.message ?: "Ukjent feil med request gjør at server ikke kan håndtere forespørselen",
                        ),
                    ),
                ),
            )
        }

        exception<IllegalArgumentException> { call, exception ->
            when (exception) {
                is NumberFormatException -> call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse.BadRequestError(
                        call.getCallId(),
                        listOf(
                            ErrorDetail(
                                detail = "Et parameter/argument i requesten din kunne ikke formateres. Sjekk at alle IDer har riktig type.",
                            ),
                        ),
                    ),
                )
            }
        }
    }
}
