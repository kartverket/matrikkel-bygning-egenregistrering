package no.kartverket.matrikkel.bygning.routes

import io.ktor.server.auth.*
import io.ktor.server.routing.*


inline fun <reified T : Any> RoutingCall.principalOrThrow(): T {
    return principal<T>() ?: throw IllegalStateException("No principal was found on the incoming call, but was needed to authenticate.")
}
