package no.kartverket.matrikkel.bygning.v1.ekstern.bygning

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.kartverket.matrikkel.bygning.TestApplicationWithDb
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.bygning.BygningEksternResponse
import org.junit.jupiter.api.Test

class BygningEksternTest : TestApplicationWithDb() {

    @Test
    fun `gitt en gyldig token med riktig scope skal tilgang gis`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()
        val token = signedJWTTokenWithScope()

        val response = client.get("/v1/ekstern/bygninger/1") {
            headers {
                append("Authorization", "Bearer ${token.serialize()}")
            }
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<BygningEksternResponse>()).all {
            prop(BygningEksternResponse::bygningId).isEqualTo(1L)
            prop(BygningEksternResponse::bruksenheter).hasSize(2)
        }
    }

    @Test
    fun `gitt et token med feil scope skal tilgang nektes`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()
        val token = signedJWTTokenWithScope("feil:scope")

        val response = client.get("/v1/ekstern/bygninger/1") {
            headers {
                append("Authorization", "Bearer ${token.serialize()}")
            }
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `gitt et kall uten token skal tilgang nektes`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/ekstern/bygninger/1")

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    private fun signedJWTTokenWithScope(scope: String = "kartverket:riktig:scope"): SignedJWT {
        val token: SignedJWT = mockOAuthServer.issueToken(
            issuerId = "testIssuer",
            subject = "123456789",
            claims = mapOf(
                "scope" to scope,
                "orgno" to "123456789",
            ),
        )
        return token
    }
}
