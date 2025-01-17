package no.kartverket.matrikkel.bygning.plugins


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


class MockJWTAuthenticationProvider(config: MockJWTConfig) : AuthenticationProvider(config) {
    override suspend fun onAuthenticate(context: AuthenticationContext) {
        context.principal(Principal("31129956715"))
    }
}

data class Principal(val pid: String)

class MockJWTConfig(name: String) : AuthenticationProvider.Config(name)

fun Application.configureAuthentication(config: ApplicationConfig) {
    install(Authentication) {
        jwtFromConfig("maskinporten", config)

        if (Env.isLocal()) {
            register(MockJWTAuthenticationProvider(MockJWTConfig("idporten")))
        } else {
            jwtFromConfig("idporten", config)
        }
    }
}


fun AuthenticationConfig.jwtFromConfig(name: String, appConfig: ApplicationConfig) {
    jwt(name) {
        val isDisabled = appConfig.property("${name}.disabled").getString().toBoolean()

        if (isDisabled) {
            log.warn("${name.toUpperCasePreservingASCIIRules()} autentisering er deaktivert! Forsikre deg om at dette ikke skjer utenfor lokale eller utviklingsmiljøer")
        }

        skipWhen { isDisabled }

        if (!isDisabled) {
            val authConfig = JWTAuthenticationConfig(
                jwksUri = appConfig.property("$name.jwksUri").getString(),
                issuer = appConfig.property("$name.issuer").getString(),
                // Vi vil jo egentlig ikke at scopes skal kunne være null for maskinporten. Hardkode at maskinporten ikke skal ha nullable scope?
                scopes = appConfig.propertyOrNull("$name.scopes")?.getString(),
            )

            val jwkProvider = JwkProviderBuilder(URI(authConfig.jwksUri).toURL())
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()

            verifier(jwkProvider, authConfig.issuer) {
                acceptLeeway(3)

                authConfig.scopes?.let { scopes ->
                    withClaim("scope", authConfig.scopes)
                }
            }

            validate { it ->
                Principal(it.payload.getClaim("pid").asString())
            }
        }
    }
}

data class JWTAuthenticationConfig(
    val jwksUri: String,
    val issuer: String,
    val scopes: String?,
)
