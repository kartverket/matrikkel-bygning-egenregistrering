package no.kartverket.matrikkel.bygning.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("maskinporten") {
            validate { credential ->
                // TODO Skal faktisk validere maskinporten
                JWTPrincipal(credential.payload)
            }
        }
    }
}
