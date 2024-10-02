package no.kartverket.matrikkel.bygning.v1.egenregistrering

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.index
import assertk.assertions.isBetween
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
import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.routes.v1.bygning.AvlopKodeResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.BruksarealResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.BruksenhetResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.ByggeaarResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.BygningResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.EnergikildeResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.MultikildeResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.OppvarmingResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.RegisterMetadataResponse
import no.kartverket.matrikkel.bygning.routes.v1.bygning.VannforsyningKodeResponse
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.AvlopRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.BruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.BruksenhetRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.ByggeaarRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.BygningRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.EnergikildeRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.OppvarmingRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.VannforsyningRegistreringRequest
import org.junit.jupiter.api.Test
import java.time.Instant

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

            assertThat(bygning).all {
                prop(BygningResponse::bruksareal).isNotNull().all {
                    prop(MultikildeResponse<BruksarealResponse>::egenregistrert).isNotNull().all {
                        prop(BruksarealResponse::data).isEqualTo(125.0)
                        prop(BruksarealResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                    }
                }

                prop(BygningResponse::byggeaar).isNotNull().all {
                    prop(MultikildeResponse<ByggeaarResponse>::egenregistrert).isNotNull().all {
                        prop(ByggeaarResponse::data).isEqualTo(2010)
                        prop(ByggeaarResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                    }
                }

                prop(BygningResponse::vannforsyning).isNotNull().all {
                    prop(MultikildeResponse<VannforsyningKodeResponse>::egenregistrert).isNotNull().all {
                        prop(VannforsyningKodeResponse::data).isEqualTo(VannforsyningKode.OffentligVannverk)
                        prop(VannforsyningKodeResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                    }
                }

                prop(BygningResponse::avlop).isNotNull().all {
                    prop(MultikildeResponse<AvlopKodeResponse>::egenregistrert).isNotNull().all {
                        prop(AvlopKodeResponse::data).isEqualTo(AvlopKode.OffentligKloakk)
                        prop(AvlopKodeResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                    }
                }

                prop(BygningResponse::bruksenheter).index(0).all {
                    prop(BruksenhetResponse::bruksenhetId).isEqualTo(1L)
                    prop(BruksenhetResponse::bruksareal).isNotNull().all {
                        prop(MultikildeResponse<BruksarealResponse>::egenregistrert).isNotNull().all {
                            prop(BruksarealResponse::data).isEqualTo(100.0)
                            prop(BruksarealResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }

                    prop(BruksenhetResponse::energikilder).isNotNull().all {
                        prop(MultikildeResponse<List<EnergikildeResponse>>::egenregistrert).isNotNull().single().all {
                            prop(EnergikildeResponse::data).isEqualTo(EnergikildeKode.Elektrisitet)
                            prop(EnergikildeResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }

                    prop(BruksenhetResponse::oppvarminger).isNotNull().all {
                        prop(MultikildeResponse<List<OppvarmingResponse>>::egenregistrert).isNotNull().single().all {
                            prop(OppvarmingResponse::data).isEqualTo(OppvarmingKode.Elektrisk)
                            prop(OppvarmingResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }
                }

                prop(BygningResponse::bruksenheter).index(1).all {
                    prop(BruksenhetResponse::bruksenhetId).isEqualTo(2L)
                    prop(BruksenhetResponse::bruksareal).isNull()
                    prop(BruksenhetResponse::energikilder).isNull()
                    prop(BruksenhetResponse::oppvarminger).isNull()
                }
            }
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

        assertThat(bruksenhetResponse.status).isEqualTo(HttpStatusCode.OK)
        val bruksenhet = bruksenhetResponse.body<BruksenhetResponse>()

        val now = Instant.now()
        assertThat(bruksenhet).all {
            prop(BruksenhetResponse::bruksenhetId).isEqualTo(1L)

            prop(BruksenhetResponse::bruksareal).isNotNull().all {
                prop(MultikildeResponse<BruksarealResponse>::egenregistrert).isNotNull().all {
                    prop(BruksarealResponse::data).isEqualTo(100.0)
                    prop(BruksarealResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
            }

            prop(BruksenhetResponse::energikilder).isNotNull().all {
                prop(MultikildeResponse<List<EnergikildeResponse>>::egenregistrert).isNotNull().single().all {
                    prop(EnergikildeResponse::data).isEqualTo(EnergikildeKode.Elektrisitet)
                    prop(EnergikildeResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
            }

            prop(BruksenhetResponse::oppvarminger).isNotNull().all {
                prop(MultikildeResponse<List<OppvarmingResponse>>::egenregistrert).isNotNull().single().all {
                    prop(OppvarmingResponse::data).isEqualTo(OppvarmingKode.Elektrisk)
                    prop(OppvarmingResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
            }
        }
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
                            bruksarealRegistrering = BruksarealRegistreringRequest(bruksareal = 120.0),
                            byggeaarRegistrering = ByggeaarRegistreringRequest(byggeaar = 2008),
                            vannforsyningRegistrering = null,
                            avlopRegistrering = null,
                        ),
                        bruksenhetRegistreringer = listOf(
                            BruksenhetRegistreringRequest(
                                bruksenhetId = 1L,
                                bruksarealRegistrering = BruksarealRegistreringRequest(bruksareal = 40.0),
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

            val now = Instant.now()
            assertThat(bygning).all {
                prop(BygningResponse::bruksareal).isNotNull().all {
                    prop(MultikildeResponse<BruksarealResponse>::egenregistrert).isNotNull().all {
                        prop(BruksarealResponse::data).isEqualTo(120.0)
                        prop(BruksarealResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                    }
                }

                prop(BygningResponse::byggeaar).isNotNull().all {
                    prop(MultikildeResponse<ByggeaarResponse>::egenregistrert).isNotNull().all {
                        prop(ByggeaarResponse::data).isEqualTo(2008)
                        prop(ByggeaarResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                    }
                }
                prop(BygningResponse::bruksenheter)
                    .extracting(BruksenhetResponse::bruksenhetId) { it.bruksareal?.egenregistrert?.data }
                    .containsExactly(1L to 40.0, 2L to null)

            }
        }

    private fun EgenregistreringRequest.Companion.validEgenregistrering() = EgenregistreringRequest(
        bygningId = 1L,
        bygningRegistrering = BygningRegistreringRequest(
            bruksarealRegistrering = BruksarealRegistreringRequest(125.0),
            byggeaarRegistrering = ByggeaarRegistreringRequest(2010),
            vannforsyningRegistrering = VannforsyningRegistreringRequest(
                VannforsyningKode.OffentligVannverk,
            ),
            avlopRegistrering = AvlopRegistreringRequest(
                avlop = AvlopKode.OffentligKloakk,
            ),
        ),
        bruksenhetRegistreringer = listOf(
            BruksenhetRegistreringRequest(
                bruksenhetId = 1L,
                bruksarealRegistrering = BruksarealRegistreringRequest(bruksareal = 100.0),
                energikildeRegistrering = EnergikildeRegistreringRequest(
                    listOf(EnergikildeKode.Elektrisitet),
                ),
                oppvarmingRegistrering = OppvarmingRegistreringRequest(
                    listOf(OppvarmingKode.Elektrisk),
                ),
            ),
        ),
    )

    private fun Assert<RegisterMetadataResponse>.hasRegistreringstidspunktWithinThreshold(now: Instant): () -> Unit {
        return {
            prop(RegisterMetadataResponse::registreringstidspunkt).isBetween(now, now.plusSeconds(1))
        }
    }
}
