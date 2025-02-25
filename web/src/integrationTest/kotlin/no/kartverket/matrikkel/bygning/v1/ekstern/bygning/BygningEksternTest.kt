package no.kartverket.matrikkel.bygning.v1.ekstern.bygning

import assertk.all
import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.kartverket.matrikkel.bygning.TestApplicationWithDb
import no.kartverket.matrikkel.bygning.application.hendelser.BygningHendelseType
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.bygning.BygningEksternResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.hendelse.HendelseContainerResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.hendelse.HendelseResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueIDPortenJWT
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueMaskinportenJWT
import no.kartverket.matrikkel.bygning.v1.common.validEgenregistreringMultipleBruksenheter
import org.junit.jupiter.api.Test

class BygningEksternTest : TestApplicationWithDb() {
    @Test
    fun `gitt en gyldig token med riktig scope skal tilgang gis`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()
        val token = mockOAuthServer.issueMaskinportenJWT()

        val response = client.get("/v1/bygninger/1") {
            bearerAuth(token.serialize())
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
        val token = mockOAuthServer.issueMaskinportenJWT("feil:scope")

        val response = client.get("/v1/bygninger/1") {
           bearerAuth(token.serialize())
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `gitt et kall uten token skal tilgang nektes`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/bygninger/1")

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `gitt at man har egenregistrert på to bruksenheter så skal det være to hendelser i hendelsesloggen`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val idportenJWT = mockOAuthServer.issueIDPortenJWT()

        client.post("/v1/intern/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest.validEgenregistreringMultipleBruksenheter(),
            )
            bearerAuth(idportenJWT.serialize())
        }

        val maskinportenJWT = mockOAuthServer.issueMaskinportenJWT()
        val result = client.get("/v1/hendelser") {
            url {
                parameters.append("antall", "10")
            }
            bearerAuth(maskinportenJWT.serialize())
        }

        val hendelseContainer = result.body<HendelseContainerResponse>()

        assertThat(hendelseContainer.hendelser).hasSize(2)
        assertThat(hendelseContainer.hendelser).each {
            it.all {
                prop(HendelseResponse::type).isEqualTo(BygningHendelseType.BRUKSENHET_OPPDATERT)
            }
        }
        assertThat(hendelseContainer.hendelser[0]).isNotNull().all {
            prop(HendelseResponse::objectId).isEqualTo(1L)
        }

        assertThat(hendelseContainer.hendelser[1]).isNotNull().all {
            prop(HendelseResponse::objectId).isEqualTo(2L)
        }
    }
}
