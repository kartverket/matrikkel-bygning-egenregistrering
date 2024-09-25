package no.kartverket.matrikkel.bygning.v1

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.kartverket.matrikkel.bygning.TestApplicationWithDb
import no.kartverket.matrikkel.bygning.routes.v1.dto.response.BygningResponse
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
}
