package no.kartverket.matrikkel.bygning.v1

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.kartverket.matrikkel.bygning.TestWithDb
import no.kartverket.matrikkel.bygning.models.Bygning
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BygningRouteTest : TestWithDb() {

    @Test
    fun `gitt at en bygning id eksisterer svarer bygning route ok`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/bygninger/1")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Bygning>().bygningId).isEqualTo("1")
        assertThat(response.body<Bygning>().bruksenheter).hasSize(2)
    }

    @Test
    fun `gitt at en bygning id ikke eksisterer svarer bygning route not found`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/bygninger/10000000")

        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
    }
}
