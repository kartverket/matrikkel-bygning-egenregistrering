package no.kartverket.matrikkel.bygning.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import no.kartverket.matrikkel.bygning.routes.v1.common.ErrorResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(object {}::class.java)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, exception ->
            when (exception) {
                is BadRequestException -> {
                    logger.warn("Bad request exception", exception)
                    when (val cause = exception.cause) {
                        is JsonConvertException -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse.BadRequestError(
                                    cause.message ?: "Ukjent feil etter serialisering av request objekt",
                                ),
                            )
                        }

                        else -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse.BadRequestError(
                                    exception.message
                                        ?: "Ukjent feil med request gjør at server ikke kan håndtere forespørselen",
                                ),
                            )
                        }
                    }
                }

                is IllegalArgumentException -> {
                    logger.warn("Illegal argument exception", exception)

                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse.BadRequestError(
                            exception.message ?: "Et eller flere felter i requesten var ugyldig",
                        ),
                    )
                }

                else -> {
                    logger.error("Uhåndtert exception kastet", exception)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse.InternalServerError(),
                    )
                }
            }
        }
    }
}
