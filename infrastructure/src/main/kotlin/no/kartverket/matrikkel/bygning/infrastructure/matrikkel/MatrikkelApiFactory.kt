package no.kartverket.matrikkel.bygning.infrastructure.matrikkel

import no.kartverket.matrikkel.bygning.application.bygning.BygningClient
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client.LocalBygningClient
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client.MatrikkelBygningClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val log: Logger = LoggerFactory.getLogger(object {}::class.java)

fun createBygningClient(
    config: MatrikkelApiConfig
): BygningClient {
    if (config.useStub) {
        log.warn("Bruker stub for matrikkel APIet. Bør kun brukes lokalt!")
        return LocalBygningClient()
    }

    return MatrikkelBygningClient(
        MatrikkelApi(
            URI(config.baseUrl),
        ).withAuth(
            config.username,
            config.password,
        ),
    )
}
