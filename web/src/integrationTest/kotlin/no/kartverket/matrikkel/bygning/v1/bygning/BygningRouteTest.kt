package no.kartverket.matrikkel.bygning.v1.bygning

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import assertk.assertions.single
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.kartverket.matrikkel.bygning.TestApplicationWithDb
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.routes.v1.bygning.AvlopKodeResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.BruksarealResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.BruksenhetSimpleResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.ByggeaarResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.BygningResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.BygningSimpleResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.EnergikildeResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.OppvarmingResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.VannforsyningKodeResponse
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.v1.common.hasRegistreringstidspunktWithinThreshold
import no.kartverket.matrikkel.bygning.v1.common.validEgenregistrering
import org.junit.jupiter.api.Test
import java.time.Instant

class BygningRouteTest : TestApplicationWithDb() {

    @Test
    fun `gitt at en bygning id eksisterer svarer bygning route ok`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/bygninger/1")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<BygningResponse>()).all {
            prop(BygningResponse::bygningId).isEqualTo(1L)
            prop(BygningResponse::bruksenheter).hasSize(2)
        }
    }

    @Test
    fun `gitt at en bygning id ikke eksisterer svarer bygning route not found`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/bygninger/10000000")

        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
    }

    @Test
    fun `gitt at en bygning eksisterer uten egenregistrert data skal alle feltene vaere null`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/bygninger/1/egenregistrert")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<BygningSimpleResponse>()).all {
            prop(BygningSimpleResponse::bruksenheter).hasSize(2)
            prop(BygningSimpleResponse::bruksenheter).index(0).all {
                prop(BruksenhetSimpleResponse::totaltBruksareal).isNull()
                prop(BruksenhetSimpleResponse::avlop).isNull()
                prop(BruksenhetSimpleResponse::byggeaar).isNull()
                prop(BruksenhetSimpleResponse::oppvarminger).isNull()
                prop(BruksenhetSimpleResponse::energikilder).isNull()
                prop(BruksenhetSimpleResponse::vannforsyning).isNull()
            }
        }
    }

    @Test
    fun `gitt at en bygning eksisterer med noe egenregistrert data feltene vaere satt`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        client.post("/v1/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest.validEgenregistrering(),
            )
        }

        val response = client.get("/v1/bygninger/1/egenregistrert")

        val now = Instant.now()

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<BygningSimpleResponse>()).all {
            prop(BygningSimpleResponse::bruksenheter).hasSize(2)
            prop(BygningSimpleResponse::bruksenheter).index(0).all {
                prop(BruksenhetSimpleResponse::totaltBruksareal).isNotNull().all {
                    prop(BruksarealResponse::data).isEqualTo(125.0)
                    prop(BruksarealResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
                prop(BruksenhetSimpleResponse::byggeaar).isNotNull().all {
                    prop(ByggeaarResponse::data).isEqualTo(2010)
                    prop(ByggeaarResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
                prop(BruksenhetSimpleResponse::vannforsyning).isNotNull().all {
                    prop(VannforsyningKodeResponse::data).isEqualTo(VannforsyningKode.OffentligVannverk)
                    prop(VannforsyningKodeResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
                prop(BruksenhetSimpleResponse::avlop).isNotNull().all {
                    prop(AvlopKodeResponse::data).isEqualTo(AvlopKode.OffentligKloakk)
                    prop(AvlopKodeResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
                prop(BruksenhetSimpleResponse::oppvarminger).isNotNull().single().all {
                    prop(OppvarmingResponse::data).isEqualTo(OppvarmingKode.Elektrisk)
                    prop(OppvarmingResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
                prop(BruksenhetSimpleResponse::energikilder).isNotNull().single().all {
                    prop(EnergikildeResponse::data).isEqualTo(EnergikildeKode.Elektrisitet)
                    prop(EnergikildeResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
            }
        }
    }
}



