package no.kartverket.matrikkel.bygning.plugins.authentication.mock

import io.ktor.server.auth.*
import no.kartverket.matrikkel.bygning.plugins.authentication.DigDirPrincipal

internal class MockJWTAuthenticationProvider(config: MockJWTConfig) : AuthenticationProvider(config) {
    override suspend fun onAuthenticate(context: AuthenticationContext) {
        context.principal(DigDirPrincipal("31129956715"))
    }
}

internal class MockJWTConfig(name: String) : AuthenticationProvider.Config(name)
