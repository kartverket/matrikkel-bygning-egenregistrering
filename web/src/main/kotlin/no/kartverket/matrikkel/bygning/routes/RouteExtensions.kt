package no.kartverket.matrikkel.bygning.routes

import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.routing.RoutingCall

/**
 * Henter ut fnr fra pid claimet i IDPorten token.
 */
fun RoutingCall.getFnr(): String = this.principal<JWTPrincipal>()?.get("pid") ?: throw BadRequestException("pid mangler i JWTCredential")
