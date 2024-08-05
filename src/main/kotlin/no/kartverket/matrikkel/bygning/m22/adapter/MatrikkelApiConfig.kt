package no.kartverket.matrikkel.bygning.m22.adapter

import no.kartverket.matrikkel.bygning.m22.BygningClient
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import java.net.URI

class MatrikkelApiConfig {

    companion object {
        fun createBygningClient(
            matrikkelBaseUrl: String,
            matrikkelUsername: String,
            matrikkelPassword: String,
        ): BygningClient {
            return MatrikkelBygningClient(
                MatrikkelApi(
                    URI(matrikkelBaseUrl),
                ).withAuth(matrikkelUsername, matrikkelPassword)
            )
        }
    }
}
