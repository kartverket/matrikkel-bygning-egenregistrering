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

            if (matrikkelUsername.isEmpty() || matrikkelPassword.isEmpty() || matrikkelBaseUrl.isEmpty()) {
                throw RuntimeException("MatrikkelConfig mangler brukernavn, passord eller base url. Dersom applikasjonen kjører i sky er disse miljøvariablene påkrevd")
            }

            return MatrikkelBygningClient(
                MatrikkelApi(
                    URI(matrikkelBaseUrl),
                ).withAuth(matrikkelUsername, matrikkelPassword)
            )
        }
    }
}