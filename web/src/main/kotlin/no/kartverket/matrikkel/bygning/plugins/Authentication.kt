package no.kartverket.matrikkel.bygning.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import no.kartverket.matrikkel.bygning.config.Env
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.concurrent.TimeUnit

private val log: Logger = LoggerFactory.getLogger(object {}::class.java)

fun Application.configureMaskinportenAuthentication(config: ApplicationConfig) {
    install(Authentication) {
        jwt("maskinporten") {
            val shouldSkipMaskinporten = shouldSkipMaskinporten(config)

            skipWhen { shouldSkipMaskinporten }

            if (!shouldSkipMaskinporten) {
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

private fun shouldSkipMaskinporten(config: ApplicationConfig): Boolean {
    val shouldSkip = if (Env.isLocal()) {
        config.propertyOrNull("maskinporten.skip")?.getString().toBoolean()
    } else {
        Env.isMaskinportenDisabled()
    }

    if (shouldSkip) {
        log.warn("Maskinporten autentisering er deaktivert! Forsikre deg om at dette ikke skjer utenfor lokale eller utviklingsmiljøer")
    }

    return shouldSkip
}

data class AuthenticationConfig(
    val jwksUri: String,
    val issuer: String,
    val requiredScopes: String,
)
