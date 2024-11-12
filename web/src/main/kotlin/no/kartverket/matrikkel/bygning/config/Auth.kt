package no.kartverket.matrikkel.bygning.config

import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth.AuthService

fun Application.configureMatrikkelAuth(authService: AuthService) {
    install(Authentication) {
        bearer("matrikkel-auth") {
            realm = "Bygning"
            authenticate { bearerTokenCredential ->
                authService.harMatrikkeltilgang(bearerTokenCredential.token)?.let { UserIdPrincipal(it) }
            }
        }
    }
}
