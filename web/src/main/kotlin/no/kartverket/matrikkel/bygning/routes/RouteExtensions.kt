package no.kartverket.matrikkel.bygning.routes

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.routing.*

/**
 * Henter ut fnr fra pid claimet i IDPorten token.
 */
fun RoutingCall.getFnr(): String {
    return this.principal<JWTPrincipal>()?.get("pid") ?: throw BadRequestException("pid mangler i JWTCredential")
}
