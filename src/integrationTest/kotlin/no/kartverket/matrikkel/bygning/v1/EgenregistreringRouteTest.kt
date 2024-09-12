package no.kartverket.matrikkel.bygning.v1

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.kartverket.matrikkel.bygning.TestApplicationWithDb
import no.kartverket.matrikkel.bygning.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.models.VannforsyningRegistrering
import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.routes.v1.dto.request.BruksenhetRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.dto.request.BygningRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.dto.request.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.dto.response.BruksenhetResponse
import no.kartverket.matrikkel.bygning.routes.v1.dto.response.BygningResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class EgenregistreringRouteTest : TestApplicationWithDb() {

    @Test
    fun `gitt at en bygning eksisterer og request er gyldig svarer egenregistrering route ok`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.post("/v1/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest.validEgenregistrering(),
            )
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
    }

    @Test
    fun `gitt at en bygning eksisterer men bruksenheter ikke eksisterer paa bygningen skal egenregistrering feile`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.post("/v1/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest.validEgenregistrering().copy(
                    bruksenhetRegistreringer = listOf(
                        BruksenhetRegistreringRequest(
                            bruksenhetId = 3L,
                            bruksarealRegistrering = null,
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                        ),
                    ),
                ),
            )
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun `gitt en gyldig egenregistrering paa bygning og bruksenhet kan bygningen hentes ut med de egenregistrerte dataene`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()

            val response = client.post("/v1/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.validEgenregistrering(),
                )
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.Created)

            val bygningResponse = client.get("/v1/bygninger/1")

            assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
            val bygning = bygningResponse.body<BygningResponse>()

            val now = Instant.now()
            assertThat(bygning.bruksareal?.data).isEqualTo(125.0)
            assertThat(bygning.bruksareal?.metadata?.registreringsTidspunkt)
                .isCloseTo(now, within(1, ChronoUnit.SECONDS))

            assertThat(bygning.byggeaar?.data).isEqualTo(2010)
            assertThat(bygning.byggeaar?.metadata?.registreringsTidspunkt)
                .isCloseTo(now, within(1, ChronoUnit.SECONDS))

            assertThat(bygning.vannforsyning?.data).isEqualTo(VannforsyningKode.OffentligVannverk)
            assertThat(bygning.vannforsyning?.metadata?.registreringsTidspunkt)
                .isCloseTo(now, within(1, ChronoUnit.SECONDS))

            assertThat(bygning.avlop?.data).isEqualTo(AvlopKode.OffentligKloakk)
            assertThat(bygning.avlop?.metadata?.registreringsTidspunkt)
                .isCloseTo(now, within(1, ChronoUnit.SECONDS))

            val bruksenhet = bygning.bruksenheter.find { it.bruksenhetId == 1L } ?: throw IllegalStateException("Fant ikke bruksenhet")
            assertThat(bruksenhet.bruksareal?.data).isEqualTo(100.0)
            assertThat(bruksenhet.bruksareal?.metadata?.registreringsTidspunkt)
                .isCloseTo(now, within(1, ChronoUnit.SECONDS))

            assertThat(bruksenhet.energikilder).satisfiesExactly(
                { energikilde ->
                    assertThat(energikilde.data).isEqualTo(EnergikildeKode.Elektrisitet)
                    assertThat(energikilde.metadata.registreringsTidspunkt).isCloseTo(now, within(1, ChronoUnit.SECONDS))
                },
            )
            assertThat(bruksenhet.oppvarminger).satisfiesExactly(
                { oppvarming ->
                    assertThat(oppvarming.data).isEqualTo(OppvarmingKode.Elektrisk)
                    assertThat(oppvarming.metadata.registreringsTidspunkt).isCloseTo(now, within(1, ChronoUnit.SECONDS))
                },
            )
        }

    @Test
    fun `gitt en gyldig egenregistrering paa bruksenhet kan bruksenheten hentes ut med de egenregistrerte dataene`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.post("/v1/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest.validEgenregistrering().copy(
                    bygningRegistrering = null,
                ),
            )
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)

        val bruksenhetResponse = client.get("/v1/bygninger/1/bruksenheter/1")

        val now = Instant.now()
        assertThat(bruksenhetResponse.status).isEqualTo(HttpStatusCode.OK)
        val bruksenhet = bruksenhetResponse.body<BruksenhetResponse>()
        assertThat(bruksenhet.bruksareal?.data).isEqualTo(100.0)
        assertThat(bruksenhet.bruksareal?.metadata?.registreringsTidspunkt)
            .isCloseTo(now, within(1, ChronoUnit.SECONDS))

        assertThat(bruksenhet.energikilder).satisfiesExactly(
            { energikilde ->
                assertThat(energikilde.data).isEqualTo(EnergikildeKode.Elektrisitet)
                assertThat(energikilde.metadata.registreringsTidspunkt).isCloseTo(now, within(1, ChronoUnit.SECONDS))
            },
        )
        assertThat(bruksenhet.oppvarminger).satisfiesExactly(
            { oppvarming ->
                assertThat(oppvarming.data).isEqualTo(OppvarmingKode.Elektrisk)
                assertThat(oppvarming.metadata.registreringsTidspunkt).isCloseTo(now, within(1, ChronoUnit.SECONDS))
            },
        )
    }

    @Test
    fun `gitt to gyldige egenregistreringer paa bygning og bruksenhet returneres dataene med den nyeste registreringen`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()

            val egenregistrering1 = client.post("/v1/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.validEgenregistrering(),
                )
            }
            assertThat(egenregistrering1.status).isEqualTo(HttpStatusCode.Created)

            val egenregistrering2 = client.post("/v1/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.validEgenregistrering().copy(
                        bygningRegistrering = BygningRegistreringRequest(
                            bruksarealRegistrering = BruksarealRegistrering(bruksareal = 120.0),
                            byggeaarRegistrering = ByggeaarRegistrering(byggeaar = 2008),
                            vannforsyningRegistrering = null,
                            avlopRegistrering = null,
                        ),
                        bruksenhetRegistreringer = listOf(
                            BruksenhetRegistreringRequest(
                                bruksenhetId = 1L,
                                bruksarealRegistrering = BruksarealRegistrering(bruksareal = 40.0),
                                energikildeRegistrering = null,
                                oppvarmingRegistrering = null,
                            ),
                        ),
                    ),
                )
            }
            assertThat(egenregistrering2.status).isEqualTo(HttpStatusCode.Created)

            val bygningResponse = client.get("/v1/bygninger/1")

            assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
            val bygning = bygningResponse.body<BygningResponse>()
            assertThat(bygning.bruksareal?.data).isEqualTo(120.0)
            assertThat(bygning.byggeaar?.data).isEqualTo(2008)

            assertThat(bygning.bruksenheter).satisfiesExactly(
                    { bruksenhet1 ->
                        assertThat(bruksenhet1.bruksenhetId).isEqualTo(1L)
                        assertThat(bruksenhet1.bruksareal?.data).isEqualTo(40.0)
                    },
                    { bruksenhet2 ->
                        assertThat(bruksenhet2.bruksenhetId).isEqualTo(2L)
                        assertThat(bruksenhet2.bruksareal).isNull()
                    },
            )
        }

    private fun EgenregistreringRequest.Companion.validEgenregistrering() = EgenregistreringRequest(
        bygningId = 1L,
        bygningRegistrering = BygningRegistreringRequest(
            bruksarealRegistrering = BruksarealRegistrering(125.0),
            byggeaarRegistrering = ByggeaarRegistrering(2010),
            vannforsyningRegistrering = VannforsyningRegistrering(
                VannforsyningKode.OffentligVannverk,
            ),
            avlopRegistrering = AvlopRegistrering(
                avlop = AvlopKode.OffentligKloakk,
            ),
        ),
        bruksenhetRegistreringer = listOf(
            BruksenhetRegistreringRequest(
                bruksenhetId = 1L,
                bruksarealRegistrering = BruksarealRegistrering(bruksareal = 100.0),
                energikildeRegistrering = EnergikildeRegistrering(
                    listOf(EnergikildeKode.Elektrisitet),
                ),
                oppvarmingRegistrering = OppvarmingRegistrering(
                    listOf(OppvarmingKode.Elektrisk),
                ),
            ),
        ),
    )
}
