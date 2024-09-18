package no.kartverket.matrikkel.bygning.plugins

import io.bkbn.kompendium.oas.serialization.KompendiumSerializersModule
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureHTTP() {
    install(ContentNegotiation) {
        json(
            Json {
                serializersModule = KompendiumSerializersModule.module
                encodeDefaults = true
//                explicitNulls = false
            },
        )
        removeIgnoredType<String>()
    }

    install(CallId) {
        generate {
            UUID.randomUUID().toString()
        }

        header(HttpHeaders.XRequestId)
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
