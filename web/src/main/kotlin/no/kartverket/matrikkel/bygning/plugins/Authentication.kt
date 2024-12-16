package no.kartverket.matrikkel.bygning.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import no.kartverket.matrikkel.bygning.config.Env
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.concurrent.TimeUnit

private val log: Logger = LoggerFactory.getLogger(object {}::class.java)

fun Application.configureMaskinportenAuthentication(config: AuthenticationConfig) {
    install(Authentication) {
        jwt("maskinporten") {
            skipWhen { config.shouldSkipAuthentication() }
            val jwkProvider = JwkProviderBuilder(URI(config.jwksUri).toURL())
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()

            verifier(jwkProvider, config.issuer) {
                acceptLeeway(3)
                withClaim("scope", config.requiredScope)
            }
            validate { it }
        }
    }
}

data class AuthenticationConfig(
    val jwksUri: String,
    val issuer: String,
    val requiredScope: String,
    private val shouldSkip: Boolean = false,
) {
    fun shouldSkipAuthentication(): Boolean {
        if (Env.isLocal() && shouldSkip) {
            log.warn("Maskinporten autentisering er deaktivert. Skal kun brukes lokalt!")
            return true
        }
        return false
    }
}
