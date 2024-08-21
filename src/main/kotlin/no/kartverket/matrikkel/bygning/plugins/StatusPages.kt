package no.kartverket.matrikkel.bygning.plugins

import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.models.responses.ErrorResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(object {}::class.java)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, exception ->
            logger.error("Noe gikk galt", exception)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse.InternalServerError(),
            )
        }

        exception<BadRequestException> { call, exception ->
            logger.warn("Bad request exception", exception)

            when (val cause = exception.cause) {
                is JsonConvertException -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse.BadRequestError(
                            details = listOf(
                                ErrorDetail(
                                    detail = cause.message ?: "Ukjent feil etter serialisering av request objekt",
                                ),
                            ),
                        ),
                    )
                }

                else -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse.BadRequestError(
                            details = listOf(
                                ErrorDetail(
                                    detail = exception.message
                                        ?: "Ukjent feil med request gjør at server ikke kan håndtere forespørselen",
                                ),
                            ),
                        ),
                    )
                }
            }
        }

        exception<IllegalArgumentException> { call, exception ->
            logger.warn("Illegal argument exception", exception)

            when (exception) {
                is NumberFormatException -> call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse.BadRequestError(
                        details = listOf(
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
