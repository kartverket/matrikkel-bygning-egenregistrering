package no.kartverket.matrikkel.bygning.v1

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.kartverket.matrikkel.bygning.TestWithDb
import no.kartverket.matrikkel.bygning.models.requests.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.requests.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EgenregistreringRouteTest : TestWithDb() {

    @Test
    fun `gitt at en bygning eksisterer og request er gyldig svarer egenregistrering route ok`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.post("/v1/bygninger/1/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest(
                    bygningRegistrering = BygningRegistrering(
                        bruksarealRegistrering = BruksarealRegistrering(bruksareal = 125.0),
                        byggeaarRegistrering = ByggeaarRegistrering(byggeaar = 2010),
                        vannforsyningRegistrering = null,
                        avlopRegistrering = null,
                    ),
                    bruksenhetRegistreringer = listOf(
                        BruksenhetRegistrering(
                            bruksenhetId = 1L,
                            bruksarealRegistrering = BruksarealRegistrering(bruksareal = 50.0),
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                        ),
                    ),
                ),
            )
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
    }

    @Test
    fun `gitt at en bygning eksisterer men bruksenheter ikke er på bygning skal egenregistrering feile`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.post("/v1/bygninger/1/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest(
                    bygningRegistrering = BygningRegistrering(
                        bruksarealRegistrering = BruksarealRegistrering(bruksareal = 125.0),
                        byggeaarRegistrering = ByggeaarRegistrering(byggeaar = 2010),
                        vannforsyningRegistrering = null,
                        avlopRegistrering = null,
                    ),
                    bruksenhetRegistreringer = listOf(
                        BruksenhetRegistrering(
                            bruksenhetId = 3L,
                            bruksarealRegistrering = BruksarealRegistrering(bruksareal = 50.0),
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                        ),
                    ),
                ),
            )
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }
}
