package no.kartverket.matrikkel.bygning.plugins.authentication

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.BearerTokenCredential
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.config.ApplicationConfig
import no.kartverket.matrikkel.bygning.config.Env
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth.AuthService
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth.Matrikkelrolle
import no.kartverket.matrikkel.bygning.plugins.authentication.ApplicationRoles.ACCESS_AS_APPLICATION
import no.kartverket.matrikkel.bygning.plugins.authentication.ApplicationRoles.BYGNING_ARKIVARISK_HISTORIKK
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.ENTRA_ID_ARKIVARISK_HISTORIKK_NAME
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.IDPORTEN_PROVIDER_NAME
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MASKINPORTEN_PROVIDER_NAME
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MATRIKKEL_AUTH_BERETTIGET_INTERESSE
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MATRIKKEL_AUTH_MED_PERSONDATA
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MATRIKKEL_AUTH_UTEN_PERSONDATA
import no.kartverket.matrikkel.bygning.plugins.authentication.mock.mockJwt
import java.net.URI
import java.util.concurrent.TimeUnit

object AuthenticationConstants {
    const val IDPORTEN_PROVIDER_NAME = "idporten"
    const val MASKINPORTEN_PROVIDER_NAME = "maskinporten"
    const val ENTRA_ID_ARKIVARISK_HISTORIKK_NAME = "entra_arkivarisk_historikk"
    const val MATRIKKEL_AUTH_BERETTIGET_INTERESSE = "matrikkel_auth_berettiget_interesse"
    const val MATRIKKEL_AUTH_UTEN_PERSONDATA = "matrikkel_auth_uten_persondata"
    const val MATRIKKEL_AUTH_MED_PERSONDATA = "matrikkel_auth_med_persondata"
}

object ApplicationRoles {
    const val ACCESS_AS_APPLICATION = "access_as_application"
    const val BYGNING_ARKIVARISK_HISTORIKK = "bygning_arkivarisk_historikk"
}

data class JWTAuthenticationConfig(
    val jwksUri: String,
    val issuer: String,
    val scopes: String? = null,
    val audience: String? = null,
) {
    val jwkProvider: JwkProvider
        get() =
            JwkProviderBuilder(URI(jwksUri).toURL())
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()
}

fun Application.configureAuthentication(
    config: ApplicationConfig,
    matrikkelAuth: AuthService,
) {
    install(Authentication) {
        jwtMaskinporten(config)
        jwtIDPorten(config)
        jwtEntraIdArkivariskHistorikk(config)
        configureMatrikkelAuth(config, matrikkelAuth)
    }
}

private fun AuthenticationConfig.jwtMaskinporten(config: ApplicationConfig) {
    if (Env.isLocal() && isProviderDisabled(config, MASKINPORTEN_PROVIDER_NAME)) {
        mockJwt(MASKINPORTEN_PROVIDER_NAME)
    } else {
        jwt(MASKINPORTEN_PROVIDER_NAME) {
            val authConfig =
                JWTAuthenticationConfig(
                    jwksUri = config.property("$name.jwksUri").getString(),
                    issuer = config.property("$name.issuer").getString(),
                    scopes = config.property("$name.scopes").getString(),
                )

            verifier(authConfig.jwkProvider, authConfig.issuer) {
                acceptLeeway(3)
                withClaim("scope", authConfig.scopes)
            }

            validate { credentials -> JWTPrincipal(credentials.payload) }
        }
    }
}

private fun AuthenticationConfig.jwtIDPorten(config: ApplicationConfig) {
    if (Env.isLocal() && isProviderDisabled(config, IDPORTEN_PROVIDER_NAME)) {
        mockJwt(IDPORTEN_PROVIDER_NAME) {
            jwtCreator {
                val token =
                    JWT
                        .create()
                        .withClaim("pid", "66860475309")
                        .sign(Algorithm.none())

                JWT.decode(token)
            }
        }
    } else {
        jwt(IDPORTEN_PROVIDER_NAME) {
            val authConfig =
                JWTAuthenticationConfig(
                    jwksUri = config.property("$name.jwksUri").getString(),
                    issuer = config.property("$name.issuer").getString(),
                    scopes = null,
                )

            verifier(authConfig.jwkProvider, authConfig.issuer) {
                acceptLeeway(3)
            }

            validate { credentials -> JWTPrincipal(credentials.payload) }
        }
    }
}

private fun AuthenticationConfig.jwtEntraIdArkivariskHistorikk(config: ApplicationConfig) {
    if (Env.isLocal() && isProviderDisabled(config, "entra")) {
        mockJwt(ENTRA_ID_ARKIVARISK_HISTORIKK_NAME)
    } else {
        jwt(ENTRA_ID_ARKIVARISK_HISTORIKK_NAME) {
            val authConfig =
                JWTAuthenticationConfig(
                    jwksUri = config.property("entra.jwksUri").getString(),
                    issuer = config.property("entra.issuer").getString(),
                    audience = config.property("entra.audience").getString(),
                )

            verifier(authConfig.jwkProvider, authConfig.issuer) {
                acceptLeeway(3)
                withAudience(authConfig.audience)
                withArrayClaim("roles", ACCESS_AS_APPLICATION, BYGNING_ARKIVARISK_HISTORIKK)
            }

            validate { credentials -> JWTPrincipal(credentials.payload) }
        }
    }
}

private fun isProviderDisabled(
    config: ApplicationConfig,
    name: String,
): Boolean = config.property("$name.disabled").getString().toBoolean()

private fun AuthenticationConfig.configureMatrikkelAuth(
    config: ApplicationConfig,
    authService: AuthService,
) {
    if (Env.isLocal() && isProviderDisabled(config, "matrikkel.oidc")) {
        // Må registrere authenticators med disse navnene, men uten noe å sjekke mot, så avslår de alt.
        val authenticate: suspend ApplicationCall.(BearerTokenCredential) -> Any? = { _ -> null }
        bearer(MATRIKKEL_AUTH_BERETTIGET_INTERESSE) {
            authenticate(authenticate)
        }
        bearer(MATRIKKEL_AUTH_UTEN_PERSONDATA) {
            authenticate(authenticate)
        }
        bearer(MATRIKKEL_AUTH_MED_PERSONDATA) {
            authenticate(authenticate)
        }
    } else {
        val authConfig =
            JWTAuthenticationConfig(
                jwksUri = config.property("matrikkel.oidc.jwksUri").getString(),
                issuer = config.property("matrikkel.oidc.issuer").getString(),
                audience = config.property("matrikkel.oidc.audience").getString(),
            )

        jwt(MATRIKKEL_AUTH_BERETTIGET_INTERESSE) {
            realm = "Bygning berettiget interesse"
            configureMatrikkelAuth(authConfig, authService, Matrikkelrolle.BerettigetInteresse)
        }
        jwt(MATRIKKEL_AUTH_UTEN_PERSONDATA) {
            realm = "Bygning uten personidentifikasjon"
            configureMatrikkelAuth(authConfig, authService, Matrikkelrolle.InnsynUtenPersondata)
        }
        jwt(MATRIKKEL_AUTH_MED_PERSONDATA) {
            realm = "Bygning med personidentifikasjon"
            configureMatrikkelAuth(authConfig, authService, Matrikkelrolle.InnsynMedPersondata)
        }
    }
}

private fun JWTAuthenticationProvider.Config.configureMatrikkelAuth(
    authConfig: JWTAuthenticationConfig,
    authService: AuthService,
    rolle: Matrikkelrolle,
) {
    verifier(authConfig.jwkProvider, authConfig.issuer) {
        acceptLeeway(3)
        withAudience(authConfig.audience)
    }
    validate { _ ->
        // Må ha det fulle tokenet for å sende videre, og det er ikke det som blir sendt som parameter.
        // Siden vi har kommet hit, så vet vi at Authorization-header er på forventet format.
        val authHeader = request.parseAuthorizationHeader() as HttpAuthHeader.Single
        val token = authHeader.blob
        authService
            .harMatrikkeltilgang(token, rolle)
            ?.let { UserIdPrincipal(it) }
    }
}
