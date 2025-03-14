package no.kartverket.matrikkel.bygning.v1.intern.bygning

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.kartverket.matrikkel.bygning.TestApplicationWithDb
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.AvlopKodeInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BruksarealInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BruksenhetSimpleResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.ByggeaarInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BygningInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BygningSimpleResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.EnergikildeInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.OppvarmingInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.VannforsyningKodeInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueIDPortenJWT
import no.kartverket.matrikkel.bygning.v1.common.gyldigRequest
import no.kartverket.matrikkel.bygning.v1.common.hasRegistreringstidspunktWithinThreshold
import org.junit.jupiter.api.Test
import java.time.Instant

class BygningRouteTest : TestApplicationWithDb() {
    @Test
    fun `gitt at en bygning id eksisterer svarer bygning route ok`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/intern/bygninger/1")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<BygningInternResponse>()).all {
            prop(BygningInternResponse::bygningId).isEqualTo(1L)
            prop(BygningInternResponse::bruksenheter).hasSize(2)
        }
    }

    @Test
    fun `gitt at en bygning id ikke eksisterer svarer bygning route not found`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/intern/bygninger/10000000")

        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
    }

    @Test
    fun `gitt at en bygning eksisterer uten egenregistrert data skal alle feltene vaere null`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/intern/bygninger/1/egenregistrert")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<BygningSimpleResponse>()).all {
            prop(BygningSimpleResponse::bruksenheter).hasSize(2)
            prop(BygningSimpleResponse::bruksenheter).index(0).all {
                prop(BruksenhetSimpleResponse::totaltBruksareal).isNull()
                prop(BruksenhetSimpleResponse::avlop).isNull()
                prop(BruksenhetSimpleResponse::byggeaar).isNull()
                prop(BruksenhetSimpleResponse::oppvarming).isEmpty()
                prop(BruksenhetSimpleResponse::energikilder).isEmpty()
                prop(BruksenhetSimpleResponse::vannforsyning).isNull()
            }
        }
    }

    @Test
    fun `gitt at en bygning eksisterer med noe egenregistrert data feltene vaere satt`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()
        val token = mockOAuthServer.issueIDPortenJWT()

        client.post("/v1/intern/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest.gyldigRequest(),
            )
            bearerAuth(token.serialize())
        }

        val response = client.get("/v1/intern/bygninger/1/egenregistrert")

        val now = Instant.now()

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<BygningSimpleResponse>()).all {
            prop(BygningSimpleResponse::bruksenheter).hasSize(2)
            prop(BygningSimpleResponse::bruksenheter).index(0).all {
                prop(BruksenhetSimpleResponse::totaltBruksareal).isNotNull().all {
                    prop(BruksarealInternResponse::data).isEqualTo(125.0)
                    prop(BruksarealInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
                prop(BruksenhetSimpleResponse::byggeaar).isNotNull().all {
                    prop(ByggeaarInternResponse::data).isEqualTo(2010)
                    prop(ByggeaarInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
                prop(BruksenhetSimpleResponse::vannforsyning).isNotNull().all {
                    prop(VannforsyningKodeInternResponse::data).isEqualTo(VannforsyningKode.OffentligVannverk)
                    prop(VannforsyningKodeInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
                prop(BruksenhetSimpleResponse::avlop).isNotNull().all {
                    prop(AvlopKodeInternResponse::data).isEqualTo(AvlopKode.OffentligKloakk)
                    prop(AvlopKodeInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
                prop(BruksenhetSimpleResponse::oppvarming).isNotNull().all {
                    index(0).all {
                        prop(OppvarmingInternResponse::data).isEqualTo(OppvarmingKode.Elektrisk)
                        prop(OppvarmingInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                    }
                }
                prop(BruksenhetSimpleResponse::energikilder).isNotNull().all {
                    index(0).all {
                        prop(EnergikildeInternResponse::data).isEqualTo(EnergikildeKode.Elektrisitet)
                        prop(EnergikildeInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                    }
                }
            }
        }
    }
}



