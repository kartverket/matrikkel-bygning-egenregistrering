package no.kartverket.matrikkel.bygning.infrastructure.matrikkel

import com.fasterxml.jackson.core.Base64Variants
import com.fasterxml.jackson.databind.ObjectMapper
import no.kartverket.matrikkel.bygning.application.bygning.BygningClient
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth.AuthService
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth.MatrikkelAuthService
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth.Matrikkelrolle
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
                return StubbedMatrikkelApiFactory
            } else {
                return RealMatrikkelApiFactory(config)
            }
        }
    }

    /**
     * Dersom sub i JWT er "stubbed-test-user-<matrikkelrolle>", så anser den brukeren for å ha rollen. Kun for testing og håndterer ikke
     * feil på noen elegant måte.
     */
    private data object StubbedMatrikkelApiFactory : MatrikkelApiFactory {
        override fun createBygningClient(): BygningClient {
            return LocalBygningClient()
        }

        override fun createAuthService(): AuthService {
            return object : AuthService {
                private val objectMapper = ObjectMapper()

                override suspend fun harMatrikkeltilgang(token: String, rolle: Matrikkelrolle): String? {
                    // Tokenet skal allerede ha blitt verifisert, så kjører quick & dirty her uten å trekke inn ekstra avhengigheter.
                    return token.split('.')
                        .drop(1)
                        .firstOrNull()
                        ?.let {
                            val jwt = Base64Variants.MODIFIED_FOR_URL.decode(it).decodeToString()
                            val rootNode = objectMapper.readTree(jwt)
                            val sub = rootNode.get("sub").textValue()
                            if (sub.equals("stubbed-test-user-${rolle.name}")) {
                                sub
                            } else {
                                null
                            }
                        }
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
