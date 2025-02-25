package no.kartverket.matrikkel.bygning.v1.ekstern.medpersondata.bygning

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.kartverket.matrikkel.bygning.TestApplicationWithDb
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.BygningMedPersondataResponse
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueMaskinportenJWT
import org.junit.jupiter.api.Test

class BygningMedPersondataTest : TestApplicationWithDb() {
    @Test
    fun `gitt en gyldig token med riktig scope skal tilgang gis`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()
        val token = mockOAuthServer.issueMaskinportenJWT()

        val response = client.get("/v1/medpersondata/bygninger/1") {
            bearerAuth(token.serialize())
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<BygningMedPersondataResponse>()).all {
            prop(BygningMedPersondataResponse::bygningId).isEqualTo(1L)
            prop(BygningMedPersondataResponse::bruksenheter).hasSize(2)
        }
    }

    @Test
    fun `gitt et token med feil scope skal tilgang nektes`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()
        val token = mockOAuthServer.issueMaskinportenJWT("feil:scope")

        val response = client.get("/v1/medpersondata/bygninger/1") {
            bearerAuth(token.serialize())
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `gitt et kall uten token skal tilgang nektes`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/medpersondata/bygninger/1")

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }
}
