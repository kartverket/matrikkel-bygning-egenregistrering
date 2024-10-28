package no.kartverket.matrikkel.bygning

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test

class InternalRouteTest : TestApplicationWithDb() {

    @Test
    fun `metrics endepunkt svarer ok`() = testApplication {
        internalModuleWithDatabaseEnvironment()

        val response = client.get("/metrics")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `healthz endepunkt svarer ok`() = testApplication {
        internalModuleWithDatabaseEnvironment()

        val response = client.get("/healthz")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }
}
