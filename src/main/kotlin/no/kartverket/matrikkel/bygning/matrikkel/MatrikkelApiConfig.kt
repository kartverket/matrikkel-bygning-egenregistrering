package no.kartverket.matrikkel.bygning.matrikkel

import io.ktor.server.config.*
import no.kartverket.matrikkel.bygning.config.Env
import no.kartverket.matrikkel.bygning.matrikkel.adapters.LocalBygningClient
import no.kartverket.matrikkel.bygning.matrikkel.adapters.MatrikkelBygningClient
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import java.net.URI

fun createBygningClient(
    config: ApplicationConfig
): BygningClient {
    return when {
        Env.isLocal() -> {
            LocalBygningClient()
        }

        else -> MatrikkelBygningClient(
            MatrikkelApi(
                URI(config.property("matrikkel.baseUrl").getString()),
            ).withAuth(
                config.property("matrikkel.username").getString(),
                config.property("matrikkel.password").getString()
            )
        )
    }
}
