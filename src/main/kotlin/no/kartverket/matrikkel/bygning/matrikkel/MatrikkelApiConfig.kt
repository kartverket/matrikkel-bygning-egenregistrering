package no.kartverket.matrikkel.bygning.matrikkel

import no.kartverket.matrikkel.bygning.config.Env
import no.kartverket.matrikkel.bygning.matrikkel.adapters.LocalBygningClient
import no.kartverket.matrikkel.bygning.matrikkel.adapters.MatrikkelBygningClient
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import java.net.URI

fun createBygningClient(
    matrikkelBaseUrl: String,
    matrikkelUsername: String,
    matrikkelPassword: String,
): BygningClient {
    return when {
        Env.isLocal() -> {
            LocalBygningClient()
        }

        else -> MatrikkelBygningClient(
            MatrikkelApi(
                URI(matrikkelBaseUrl),
            ).withAuth(matrikkelUsername, matrikkelPassword),
        )
    }
}
