package no.kartverket.matrikkel.bygning.plugins.authentication.mock

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.util.toUpperCasePreservingASCIIRules
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger(object {}::class.java)

internal fun AuthenticationConfig.mockJwt(
    name: String,
    configure: MockJWTConfig.() -> Unit = { MockJWTConfig(name) },
) {
    log.warn("${name.toUpperCasePreservingASCIIRules()} autentisering er deaktivert! Dette skal kun gjøres lokalt.")
    val config = MockJWTConfig(name).apply(configure)
    register(MockJWTAuthenticationProvider(config))
}

internal class MockJWTConfig(
    name: String,
) : AuthenticationProvider.Config(name) {
    internal var jwtCreator: () -> Payload = {
        val token =
            JWT
                .create()
                .sign(Algorithm.none())

        JWT.decode(token)
    }

    fun jwtCreator(jwtCreator: () -> Payload) {
        this.jwtCreator = jwtCreator
    }
}

private class MockJWTAuthenticationProvider(
    config: MockJWTConfig,
) : AuthenticationProvider(config) {
    private val jwtCreator: () -> Payload = config.jwtCreator

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        context.principal(JWTPrincipal(this.jwtCreator()))
    }
}
