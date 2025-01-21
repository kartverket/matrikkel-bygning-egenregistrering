package no.kartverket.matrikkel.bygning.plugins


import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.util.*
import no.kartverket.matrikkel.bygning.config.Env
import no.kartverket.matrikkel.bygning.plugins.AuthenticationConstants.IDPORTEN_PROVIDER_NAME
import no.kartverket.matrikkel.bygning.plugins.AuthenticationConstants.MASKINPORTEN_PROVIDER_NAME
import no.kartverket.matrikkel.bygning.plugins.AuthenticationConstants.VALID_FNR_LOCAL
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.concurrent.TimeUnit

private val log: Logger = LoggerFactory.getLogger(object {}::class.java)

object AuthenticationConstants {
    const val IDPORTEN_PROVIDER_NAME = "idporten"
    const val MASKINPORTEN_PROVIDER_NAME = "maskinporten"
    const val VALID_FNR_LOCAL = "31129956715"
}

data class DigDirPrincipal(
    /**
     * Representerer PID/FNr for enkeltpersoner i tokens fra ID-porten,
     * og OrgNr i tokens fra Maskinporten
     */
    val id: String
)

class MockJWTConfig(name: String) : AuthenticationProvider.Config(name)

class MockJWTAuthenticationProvider(config: MockJWTConfig) : AuthenticationProvider(config) {
    override suspend fun onAuthenticate(context: AuthenticationContext) {
        context.principal(DigDirPrincipal(VALID_FNR_LOCAL))
    }
}

data class JWTAuthenticationConfig(
    val jwksUri: String,
    val issuer: String,
    val scopes: String?,
) {
    val jwkProvider: JwkProvider
        get() = JwkProviderBuilder(URI(jwksUri).toURL())
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
}

fun Application.configureAuthentication(config: ApplicationConfig) {
    install(Authentication) {
        jwtMaskinporten(config)

        jwtIDPorten(config)
    }
}

private fun AuthenticationConfig.jwtMaskinporten(config: ApplicationConfig) {
    jwt(MASKINPORTEN_PROVIDER_NAME) {
        val isDisabled = isProviderDisabled(config, MASKINPORTEN_PROVIDER_NAME)

        skipWhen { isDisabled }

        if (!isDisabled) {
            val authConfig = JWTAuthenticationConfig(
                jwksUri = config.property("$name.jwksUri").getString(),
                issuer = config.property("$name.issuer").getString(),
                scopes = config.property("$name.scopes").getString(),
            )

            verifier(authConfig.jwkProvider, authConfig.issuer) {
                acceptLeeway(3)
                withClaim("scope", authConfig.scopes)
            }

            validate { DigDirPrincipal(it.payload.getClaim("orgno").asString()) }
        } else {
            warnDisabledProvider(MASKINPORTEN_PROVIDER_NAME)
        }
    }
}


private fun AuthenticationConfig.jwtIDPorten(config: ApplicationConfig) {
    if (Env.isLocal() && isProviderDisabled(config, IDPORTEN_PROVIDER_NAME)) {
        warnDisabledProvider(IDPORTEN_PROVIDER_NAME)

        register(
            MockJWTAuthenticationProvider(
                MockJWTConfig(IDPORTEN_PROVIDER_NAME),
            ),
        )
    } else {
        jwt(IDPORTEN_PROVIDER_NAME) {
            val authConfig = JWTAuthenticationConfig(
                jwksUri = config.property("$name.jwksUri").getString(),
                issuer = config.property("$name.issuer").getString(),
                scopes = null,
            )

            verifier(authConfig.jwkProvider, authConfig.issuer) {
                acceptLeeway(3)
            }

            validate { DigDirPrincipal(it.payload.getClaim("pid").asString()) }
        }
    }
}

private fun isProviderDisabled(config: ApplicationConfig, name: String): Boolean =
    config.property("${name}.disabled").getString().toBoolean()


private fun warnDisabledProvider(name: String) =
    log.warn("${name.toUpperCasePreservingASCIIRules()} autentisering er deaktivert! Forsikre deg om at dette ikke skjer utenfor lokale eller utviklingsmiljøer")
