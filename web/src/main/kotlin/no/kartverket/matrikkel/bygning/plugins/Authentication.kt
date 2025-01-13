package no.kartverket.matrikkel.bygning.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.concurrent.TimeUnit

private val log: Logger = LoggerFactory.getLogger(object {}::class.java)

fun Application.configureMaskinportenAuthentication(config: ApplicationConfig) {
    install(Authentication) {
        jwt("maskinporten") {
            val shouldDisableMaskinporten = shouldDisableMaskinporten(config)

            skipWhen { shouldDisableMaskinporten }

            if (!shouldDisableMaskinporten) {
                val authConfig = AuthenticationConfig(
                    jwksUri = config.property("maskinporten.jwksUri").getString(),
                    issuer = config.property("maskinporten.issuer").getString(),
                    requiredScopes = config.property("maskinporten.scopes").getString(),
                )

                val jwkProvider = JwkProviderBuilder(URI(authConfig.jwksUri).toURL())
                    .cached(10, 24, TimeUnit.HOURS)
                    .rateLimited(10, 1, TimeUnit.MINUTES)
                    .build()

                verifier(jwkProvider, authConfig.issuer) {
                    acceptLeeway(3)
                    withClaim("scope", authConfig.requiredScopes)
                }

                validate { it }

            }
        }
    }
}

private fun shouldDisableMaskinporten(config: ApplicationConfig): Boolean {
    val shouldDisable = config.propertyOrNull("maskinporten.disabled")?.getString().toBoolean()

    if (shouldDisable) {
        log.warn("Maskinporten autentisering er deaktivert! Forsikre deg om at dette ikke skjer utenfor lokale eller utviklingsmiljøer")
    }

    return shouldDisable
}

data class AuthenticationConfig(
    val jwksUri: String,
    val issuer: String,
    val requiredScopes: String,
)
