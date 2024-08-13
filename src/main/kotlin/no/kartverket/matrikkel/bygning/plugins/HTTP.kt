package no.kartverket.matrikkel.bygning.plugins

import io.bkbn.kompendium.oas.serialization.KompendiumSerializersModule
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.models.responses.ErrorResponse

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureHTTP() {
    install(ContentNegotiation) {
        json(
            Json {
                serializersModule = KompendiumSerializersModule.module
                encodeDefaults = true
                explicitNulls = false
            },
        )
        removeIgnoredType<String>()
    }

    fun ApplicationCall.getCallId(): String? {
        return this.request.header(HttpHeaders.XRequestId)
    }

    install(CallId) {
        retrieveFromHeader(HttpHeaders.XRequestId)
    }

    install(StatusPages) {
        exception<BadRequestException> { call, badRequestException ->
            val jsonConvertException = badRequestException.cause as? JsonConvertException
            val jsonConvertCause = jsonConvertException?.cause


            // TODO Prøve å hente ut informasjon om felt og sånt via feilmelding?
            if (jsonConvertCause?.message != null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse.BadRequestError(
                        call.getCallId(),
                        listOf(
                            ErrorDetail(
                                detail = jsonConvertCause.message!!,
                            ),
                        ),
                    ),
                )
            }

            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse.BadRequestError(
                    call.getCallId(),
                    listOf(
                        ErrorDetail(
                            detail = badRequestException.message!!,
                        ),
                    ),
                ),
            )
        }
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }

    install(CallLogging) {
        filter { call ->
            call.request.path().startsWith("/v1")
        }

        callIdMdc("call-id")
    }
}
