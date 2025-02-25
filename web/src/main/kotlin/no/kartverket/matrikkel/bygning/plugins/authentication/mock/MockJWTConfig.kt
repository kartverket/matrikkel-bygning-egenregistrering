package no.kartverket.matrikkel.bygning.plugins.authentication.mock

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

internal fun AuthenticationConfig.mockJwtPrincipalForProvider(
    name: String,
    configure: MockJWTConfig.() -> Unit = { MockJWTConfig(name) }
) {

    val config = MockJWTConfig(name).apply(configure)
    register(MockJWTAuthenticationProvider(config))
}

internal class MockJWTConfig(name: String) : AuthenticationProvider.Config(name) {
    internal var jwtCreator: () -> Payload = {
        val token = JWT.create()
            .sign(Algorithm.none())

        JWT.decode(token)
    }

    fun jwtCreator(jwtCreator: () -> Payload) {
        this.jwtCreator = jwtCreator
    }
}

private class MockJWTAuthenticationProvider(config: MockJWTConfig) : AuthenticationProvider(config) {
    private val jwtCreator: () -> Payload = config.jwtCreator

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        context.principal(JWTPrincipal(this.jwtCreator()))
    }
}
