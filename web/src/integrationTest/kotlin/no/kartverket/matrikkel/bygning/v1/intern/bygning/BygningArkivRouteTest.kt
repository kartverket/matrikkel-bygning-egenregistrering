package no.kartverket.matrikkel.bygning.v1.intern.bygning

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.kartverket.matrikkel.bygning.TestApplicationWithDb
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BruksarealInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BruksenhetSimpleResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BygningSimpleResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueM2MEntraJwt
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueIDPortenJWT
import no.kartverket.matrikkel.bygning.v1.common.validEgenregistrering
import org.junit.jupiter.api.Test
import java.time.Instant

class BygningArkivRouteTest : TestApplicationWithDb() {

    @Test
    fun `gitt manglende entra jwt token svarer arkiv med 401`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/intern/arkiv/bygninger/1/egenregistrert?registreringstidspunkt=${Instant.now()}")
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `gitt entra jwt token med ingen roller svarer arkiv med 401`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val entraIdToken = mockOAuthServer.issueM2MEntraJwt(roles = emptyList())
        val response = client.get("/v1/intern/arkiv/bygninger/1/egenregistrert?registreringstidspunkt=${Instant.now()}") {
            bearerAuth(entraIdToken.serialize())
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `gitt entra jwt token med feil rolle svarer arkiv med 401`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val entraIdToken = mockOAuthServer.issueM2MEntraJwt(roles = listOf("feil_rolle"))
        val response = client.get("/v1/intern/arkiv/bygninger/1/egenregistrert?registreringstidspunkt=${Instant.now()}") {
            bearerAuth(entraIdToken.serialize())
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `gitt entra jwt token med feil audience svarer arkiv med 401`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val entraIdToken = mockOAuthServer.issueM2MEntraJwt(audience = "feil_audience")
        val response = client.get("/v1/intern/arkiv/bygninger/1/egenregistrert?registreringstidspunkt=${Instant.now()}") {
            bearerAuth(entraIdToken.serialize())
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `gitt et ugyldig dato query parameter svarer bygning arkiv bad request`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val entraIdToken = mockOAuthServer.issueM2MEntraJwt()
        val response = client.get("/v1/intern/arkiv/bygninger/1/egenregistrert?registreringstidspunkt=UGYLDIG_DATO") {
            bearerAuth(entraIdToken.serialize())
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun `gitt et gyldig dato query parameter svarer bygning arkiv ok`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        client.post("/v1/intern/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest.validEgenregistrering(),
            )
            bearerAuth(mockOAuthServer.issueIDPortenJWT().serialize())
        }

        val entraIdToken = mockOAuthServer.issueM2MEntraJwt()
        val currentData = client.get("/v1/intern/arkiv/bygninger/1/egenregistrert?registreringstidspunkt=${Instant.now()}") {
            bearerAuth(entraIdToken.serialize())
        }
        assertThat(currentData.status).isEqualTo(HttpStatusCode.OK)
        assertThat(currentData.body<BygningSimpleResponse>()).all {
            prop(BygningSimpleResponse::bruksenheter).hasSize(2)
            prop(BygningSimpleResponse::bruksenheter).index(0).all {
                prop(BruksenhetSimpleResponse::totaltBruksareal).isNotNull().all {
                    prop(BruksarealInternResponse::data).isEqualTo(125.0)
                }
            }
        }

        val oldData = client.get("/v1/intern/arkiv/bygninger/1/egenregistrert?registreringstidspunkt=2025-01-01T15:15:47.361080Z") {
            bearerAuth(entraIdToken.serialize())
        }
        assertThat(oldData.status).isEqualTo(HttpStatusCode.OK)
        assertThat(oldData.body<BygningSimpleResponse>()).all {
            prop(BygningSimpleResponse::bruksenheter).hasSize(2)
            prop(BygningSimpleResponse::bruksenheter).index(0).all {
                prop(BruksenhetSimpleResponse::totaltBruksareal).isNull()
            }
        }
    }
}
