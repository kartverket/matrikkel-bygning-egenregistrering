package no.kartverket.matrikkel.bygning.v1

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.kartverket.matrikkel.bygning.TestWithDb
import no.kartverket.matrikkel.bygning.routes.v1.dto.response.BygningResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BygningRouteTest : TestWithDb() {

    @Test
    fun `gitt at en bygning id eksisterer svarer bygning route ok`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/bygninger/1")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<BygningResponse>().bygningId).isEqualTo(1L)
        assertThat(response.body<BygningResponse>().bruksenheter).hasSize(2)
    }

    @Test
    fun `gitt at en bygning id ikke eksisterer svarer bygning route not found`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/bygninger/10000000")

        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
    }
}
