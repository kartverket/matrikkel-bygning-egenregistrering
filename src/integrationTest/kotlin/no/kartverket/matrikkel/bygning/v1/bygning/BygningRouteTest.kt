package no.kartverket.matrikkel.bygning.v1.bygning

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
import no.kartverket.matrikkel.bygning.routes.v1.bygning.BruksenhetSimpleResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.BygningResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.BygningSimpleResponse
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.v1.common.validEgenregistrering
import org.junit.jupiter.api.Test

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
                prop(BruksenhetSimpleResponse::bruksareal).isNull()
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

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<BygningSimpleResponse>()).all {
            prop(BygningSimpleResponse::bruksenheter).hasSize(2)
            prop(BygningSimpleResponse::bruksenheter).index(0).all {
                prop(BruksenhetSimpleResponse::bruksareal).isNotNull()
                prop(BruksenhetSimpleResponse::avlop).isNotNull()
                prop(BruksenhetSimpleResponse::byggeaar).isNotNull()
                prop(BruksenhetSimpleResponse::oppvarminger).isNotNull()
                prop(BruksenhetSimpleResponse::energikilder).isNotNull()
                prop(BruksenhetSimpleResponse::vannforsyning).isNotNull()
            }
        }
    }
}



