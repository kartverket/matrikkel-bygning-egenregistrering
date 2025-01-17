package no.kartverket.matrikkel.bygning.plugins


import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.util.*
import no.kartverket.matrikkel.bygning.config.Env
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.concurrent.TimeUnit

private val log: Logger = LoggerFactory.getLogger(object {}::class.java)

fun Application.configureAuthentication(config: ApplicationConfig) {
    install(Authentication) {
        jwt("maskinporten") {
            val isDisabled = isProviderDisabled(config, name!!)

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

                validate { it ->
                    DigDirJWTPrincipal(it.payload.getClaim("orgno").asString())
                }
            }
        }

        jwtIdporten(config)
    }

}

data class DigDirJWTPrincipal(
    /**
     * Representerer PID/FNr for enkeltpersoner i tokens fra ID-porten,
     * og OrgNr i tokens fra Maskinporten
     */
    val id: String
)

class MockJWTConfig(name: String) : AuthenticationProvider.Config(name)

class MockJWTAuthenticationProvider(config: MockJWTConfig) : AuthenticationProvider(config) {
    override suspend fun onAuthenticate(context: AuthenticationContext) {
        context.principal(DigDirJWTPrincipal("31129956715"))
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

private fun AuthenticationConfig.jwtIdporten(config: ApplicationConfig) {
    val providerName = "idporten"

    val isDisabled = isProviderDisabled(config, providerName)

    if (Env.isLocal() && isDisabled) {
        register(
            MockJWTAuthenticationProvider(
                MockJWTConfig("idporten"),
            ),
        )
    } else {
        jwt("idporten") {
            skipWhen { isDisabled }

            if (!isDisabled) {
                val authConfig = JWTAuthenticationConfig(
                    jwksUri = config.property("$name.jwksUri").getString(),
                    issuer = config.property("$name.issuer").getString(),
                    scopes = null,
                )

                verifier(authConfig.jwkProvider, authConfig.issuer) {
                    acceptLeeway(3)
                }

                validate { it ->
                    DigDirJWTPrincipal(it.payload.getClaim("pid").asString())
                }
            }
        }
    }
}

private fun isProviderDisabled(config: ApplicationConfig, name: String): Boolean {
    val isDisabled = config.property("${name}.disabled").getString().toBoolean()

    if (isDisabled) {
        log.warn("${name.toUpperCasePreservingASCIIRules()} autentisering er deaktivert! Forsikre deg om at dette ikke skjer utenfor lokale eller utviklingsmiljøer")
    }

    return isDisabled
}
