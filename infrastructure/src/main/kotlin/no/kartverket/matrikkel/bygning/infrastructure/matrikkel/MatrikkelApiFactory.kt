package no.kartverket.matrikkel.bygning.infrastructure.matrikkel

import no.kartverket.matrikkel.bygning.application.bygning.BygningClient
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth.AuthService
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth.MatrikkelAuthService
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client.LocalBygningClient
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client.MatrikkelBygningClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

sealed interface MatrikkelApiFactory {
    fun createBygningClient(): BygningClient
    fun createAuthService(): AuthService

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java.enclosingClass)

        operator fun invoke(config: MatrikkelApiConfig): MatrikkelApiFactory {
            if (config.useStub) {
                log.warn("Bruker stub for matrikkel APIet. Bør kun brukes lokalt!")
                return StubbedMatrikkelApiFactory()
            } else {
                return RealMatrikkelApiFactory(config)
            }
        }
    }

    private class StubbedMatrikkelApiFactory : MatrikkelApiFactory {
        override fun createBygningClient(): BygningClient {
            return LocalBygningClient()
        }

        override fun createAuthService(): AuthService {
            return object : AuthService {
                override suspend fun harMatrikkeltilgang(token: String): String {
                    return "stubbed-test-user"
                }
            }
        }
    }

    private class RealMatrikkelApiFactory(private val config: MatrikkelApiConfig) : MatrikkelApiFactory {
        private val api = MatrikkelApi(
            URI(config.baseUrl),
        )

        override fun createBygningClient(): BygningClient {
            return MatrikkelBygningClient(
                api.withAuth(
                    config.username,
                    config.password,
                ),
            )
        }

        override fun createAuthService(): AuthService {
            return MatrikkelAuthService(api)
        }
    }
}
