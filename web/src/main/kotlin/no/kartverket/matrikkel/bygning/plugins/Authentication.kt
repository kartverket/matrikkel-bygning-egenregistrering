package no.kartverket.matrikkel.bygning.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.net.URI
import java.util.concurrent.TimeUnit


fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("maskinporten") {
            val jwkProvider =
                JwkProviderBuilder(URI("https://test.maskinporten.no/jwk").toURL())
                    .cached(10, 24, TimeUnit.HOURS)
                    .rateLimited(10, 1, TimeUnit.MINUTES)
                    .build()

            verifier(jwkProvider, "https://test.maskinporten.no/") {
                acceptLeeway(3)
            }
            validate { credential ->

                JWTPrincipal(credential.payload)
            }
        }
    }
}
