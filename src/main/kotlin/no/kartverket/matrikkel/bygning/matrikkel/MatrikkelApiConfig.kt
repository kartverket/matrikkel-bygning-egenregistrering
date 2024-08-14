package no.kartverket.matrikkel.bygning.matrikkel

import io.ktor.server.config.*
import no.kartverket.matrikkel.bygning.config.Env
import no.kartverket.matrikkel.bygning.matrikkel.adapters.LocalBygningClient
import no.kartverket.matrikkel.bygning.matrikkel.adapters.MatrikkelBygningClient
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val log: Logger = LoggerFactory.getLogger(object {}::class.java)

fun createBygningClient(
    config: ApplicationConfig
): BygningClient {
    if (Env.isLocal() && config.propertyOrNull("matrikkel.useStub")?.getString().toBoolean()) {
        log.warn("Bruker stub for matrikkel APIet. Skal kun brukes lokalt!")
        return LocalBygningClient()
    }

    return MatrikkelBygningClient(
        MatrikkelApi(
            URI(config.property("matrikkel.baseUrl").getString()),
        ).withAuth(
            config.property("matrikkel.username").getString(),
            config.property("matrikkel.password").getString(),
        ),
    )
}
