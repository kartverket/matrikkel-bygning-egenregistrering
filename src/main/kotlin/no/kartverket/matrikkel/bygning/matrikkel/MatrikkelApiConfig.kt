package no.kartverket.matrikkel.bygning.matrikkel

import no.kartverket.matrikkel.bygning.matrikkel.adapters.LocalBygningClient
import no.kartverket.matrikkel.bygning.matrikkel.adapters.MatrikkelBygningClient
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import java.net.URI

class MatrikkelApiConfig {
    companion object {
        fun createBygningClient(
            matrikkelBaseUrl: String,
            matrikkelUsername: String,
            matrikkelPassword: String,
            isDevelopment: Boolean,
        ): BygningClient {
            if (isDevelopment) {
                return LocalBygningClient()
            }

            return MatrikkelBygningClient(
                MatrikkelApi(
                    URI(matrikkelBaseUrl),
                ).withAuth(matrikkelUsername, matrikkelPassword)
            )
        }
    }
}