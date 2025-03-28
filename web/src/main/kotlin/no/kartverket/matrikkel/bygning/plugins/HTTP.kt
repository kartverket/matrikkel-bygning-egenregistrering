package no.kartverket.matrikkel.bygning.plugins

import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.path
import kotlinx.serialization.json.Json
import java.util.UUID

fun Application.configureHTTP() {
    install(ContentNegotiation) {
        json(
            Json {
                encodeDefaults = true
                explicitNulls = false
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

        callIdMdc("request_id")
    }
}
