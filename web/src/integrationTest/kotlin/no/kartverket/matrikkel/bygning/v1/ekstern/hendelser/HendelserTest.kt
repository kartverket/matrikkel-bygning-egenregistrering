package no.kartverket.matrikkel.bygning.v1.ekstern.hendelser

import assertk.all
import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import no.kartverket.matrikkel.bygning.TestApplicationWithDb
import no.kartverket.matrikkel.bygning.application.hendelser.BygningHendelseType
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.hendelse.HendelseContainerResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.hendelse.HendelseResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueIDPortenJWT
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueMaskinportenJWT
import no.kartverket.matrikkel.bygning.v1.common.gyldigRequest
import org.junit.jupiter.api.Test

class HendelserTest : TestApplicationWithDb() {
    @Test
    fun `gitt at man har egenregistrert to ganger så skal det være to hendelser i hendelsesloggen`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()

            val idportenJWT = mockOAuthServer.issueIDPortenJWT()

            client.post("/v1/intern/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.Companion.gyldigRequest(),
                )
                bearerAuth(idportenJWT.serialize())
            }

            client.post("/v1/intern/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.Companion.gyldigRequest(2L),
                )
                bearerAuth(idportenJWT.serialize())
            }

            val maskinportenJWT = mockOAuthServer.issueMaskinportenJWT()
            val result =
                client.get("/v1/hendelser") {
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
