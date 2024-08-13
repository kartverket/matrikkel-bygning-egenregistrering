package no.kartverket.matrikkel.bygning.plugins

import io.bkbn.kompendium.oas.serialization.KompendiumSerializersModule
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService

inline fun <reified T : Any> RequestValidationConfig.addValidator(
    noinline validator: suspend (T) -> ValidationResult
) {
    validate<T>(validator)
}

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureHTTP() {
    install(ContentNegotiation) {
        json(Json {
            serializersModule = KompendiumSerializersModule.module
            encodeDefaults = true
            explicitNulls = false
        })
        removeIgnoredType<String>()
    }

    install(RequestValidation) {
        addValidator(EgenregistreringsService.validateEgenregistreringRequest())
    }

    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
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
    }
}
