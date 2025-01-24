package no.kartverket.matrikkel.bygning.v1.intern.egenregistrering

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import assertk.assertions.single
import assertk.assertions.size
import assertk.assertions.support.appendName
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.kartverket.matrikkel.bygning.TestApplicationWithDb
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.routes.v1.common.ErrorResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.AvlopKodeResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BruksarealResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BruksenhetResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.ByggeaarResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BygningResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.EnergikildeResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.MultikildeResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.OppvarmingResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.RegisterMetadataResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.VannforsyningKodeResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.BruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.BruksenhetRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.ByggeaarRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueIDPortenJWT
import no.kartverket.matrikkel.bygning.v1.common.hasRegistreringstidspunktWithinThreshold
import no.kartverket.matrikkel.bygning.v1.common.ugyldigEgenregistreringMedKunBruksarealPerEtasje
import no.kartverket.matrikkel.bygning.v1.common.validEgenregistrering
import org.junit.jupiter.api.Test
import java.time.Instant

class EgenregistreringRouteTest : TestApplicationWithDb() {

    @Test
    fun `gitt at en bygning eksisterer og request er gyldig svarer egenregistrering route ok`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()
        val token = mockOAuthServer.issueIDPortenJWT()

        val response = client.post("/v1/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest.validEgenregistrering(),
            )
            headers {
                append("Authorization", "Bearer ${token.serialize()}")
            }
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
    }

    @Test
    fun `gitt en gyldig egenregistrering paa bygning og bruksenhet kan bygningen hentes ut med de egenregistrerte dataene`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueIDPortenJWT()

            val response = client.post("/v1/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.validEgenregistrering(),
                )
                headers {
                    append("Authorization", "Bearer ${token.serialize()}")
                }
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.Created)

            val bygningResponse = client.get("/v1/bygninger/1")

            assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
            val bygning = bygningResponse.body<BygningResponse>()

            val now = Instant.now()


            assertThat(bygning).all {
                prop(BygningResponse::bruksenheter).index(0).all {
                    prop(BruksenhetResponse::bruksenhetId).isEqualTo(1L)
                    prop(BruksenhetResponse::totaltBruksareal).isNotNull().all {
                        prop(MultikildeResponse<BruksarealResponse>::egenregistrert).isNotNull().all {
                            prop(BruksarealResponse::data).isEqualTo(125.0)
                            prop(BruksarealResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }

                    prop(BruksenhetResponse::byggeaar).isNotNull().all {
                        prop(MultikildeResponse<ByggeaarResponse>::egenregistrert).isNotNull().all {
                            prop(ByggeaarResponse::data).isEqualTo(2010)
                            prop(ByggeaarResponse::metadata).all {
                                hasRegistreringstidspunktWithinThreshold(now)
                                prop(RegisterMetadataResponse::kildemateriale).isEqualTo(KildematerialeKode.Selvrapportert)
                            }
                        }
                    }

                    prop(BruksenhetResponse::vannforsyning).isNotNull().all {
                        prop(MultikildeResponse<VannforsyningKodeResponse>::egenregistrert).isNotNull().all {
                            prop(VannforsyningKodeResponse::data).isEqualTo(VannforsyningKode.OffentligVannverk)
                            prop(VannforsyningKodeResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }

                    prop(BruksenhetResponse::avlop).isNotNull().all {
                        prop(MultikildeResponse<AvlopKodeResponse>::egenregistrert).isNotNull().all {
                            prop(AvlopKodeResponse::data).isEqualTo(AvlopKode.OffentligKloakk)
                            prop(AvlopKodeResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
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
                    prop(BruksenhetResponse::totaltBruksareal).isNull()
                    prop(BruksenhetResponse::energikilder).isNull()
                    prop(BruksenhetResponse::oppvarminger).isNull()
                }
            }
        }

    @Test
    fun `gitt en gyldig egenregistrering paa bruksenhet kan bruksenheten hentes ut med de egenregistrerte dataene`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()
        val token = mockOAuthServer.issueIDPortenJWT()

        val response = client.post("/v1/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest.validEgenregistrering(),
            )
            headers {
                append("Authorization", "Bearer ${token.serialize()}")
            }
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)

        val bruksenhetResponse = client.get("/v1/bygninger/1/bruksenheter/1")

        assertThat(bruksenhetResponse.status).isEqualTo(HttpStatusCode.OK)
        val bruksenhet = bruksenhetResponse.body<BruksenhetResponse>()

        val now = Instant.now()
        assertThat(bruksenhet).all {
            prop(BruksenhetResponse::bruksenhetId).isEqualTo(1L)

            prop(BruksenhetResponse::totaltBruksareal).isNotNull().all {
                prop(MultikildeResponse<BruksarealResponse>::egenregistrert).isNotNull().all {
                    prop(BruksarealResponse::data).isEqualTo(125.0)
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
            val token = mockOAuthServer.issueIDPortenJWT()

            val egenregistrering1 = client.post("/v1/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.validEgenregistrering(),
                )
                headers {
                    append("Authorization", "Bearer ${token.serialize()}")
                }
            }
            assertThat(egenregistrering1.status).isEqualTo(HttpStatusCode.Created)

            val egenregistrering2 = client.post("/v1/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.validEgenregistrering().copy(
                        bruksenhetRegistreringer = listOf(
                            BruksenhetRegistreringRequest(
                                bruksenhetId = 1L,
                                bruksarealRegistrering = BruksarealRegistreringRequest(
                                    totaltBruksareal = 40.0,
                                    etasjeRegistreringer = null,
                                    kildemateriale = null,
                                ),
                                byggeaarRegistrering = ByggeaarRegistreringRequest(
                                    byggeaar = 2008,
                                    kildemateriale = KildematerialeKode.AnnenDokumentasjon,
                                ),
                                vannforsyningRegistrering = null,
                                avlopRegistrering = null,
                                energikildeRegistrering = null,
                                oppvarmingRegistrering = null,
                            ),
                        ),
                    ),
                )
                headers {
                    append("Authorization", "Bearer ${token.serialize()}")
                }
            }
            assertThat(egenregistrering2.status).isEqualTo(HttpStatusCode.Created)

            val bygningResponse = client.get("/v1/bygninger/1")

            assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
            val bygning = bygningResponse.body<BygningResponse>()

            val now = Instant.now()
            assertThat(bygning).prop(BygningResponse::bruksenheter).all {
                withBruksenhetId(1L).all {
                    prop(BruksenhetResponse::byggeaar).isNotNull().all {
                        prop(MultikildeResponse<ByggeaarResponse>::egenregistrert).isNotNull().all {
                            prop(ByggeaarResponse::data).isEqualTo(2008)
                            prop(ByggeaarResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }
                    prop(BruksenhetResponse::totaltBruksareal).isNotNull().all {
                        prop(MultikildeResponse<BruksarealResponse>::egenregistrert).isNotNull().all {
                            prop(BruksarealResponse::data).isEqualTo(40.0)
                            prop(BruksarealResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }
                }
                withBruksenhetId(2L).all {
                    prop(BruksenhetResponse::byggeaar).isNull()
                    prop(BruksenhetResponse::totaltBruksareal).isNull()
                }
            }
        }

    @Test
    fun `gitt at egenregistrering sender info om hvem har registrert blir dette lagret og sendt ut igjen`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueIDPortenJWT()

            val response = client.post("/v1/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                        EgenregistreringRequest.validEgenregistrering(),
                )
                headers {
                    append("Authorization", "Bearer ${token.serialize()}")
                }
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.Created)

            val bygningResponse = client.get("/v1/bygninger/1")

            assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
            val bygning = bygningResponse.body<BygningResponse>()

            assertThat(bygning).all {
                prop(BygningResponse::bruksareal).isNotNull().prop(MultikildeResponse<BruksarealResponse>::egenregistrert).isNull()
                prop(BygningResponse::bruksenheter).withBruksenhetId(1L)
                    .prop(BruksenhetResponse::totaltBruksareal).isNotNull().all {
                        prop(MultikildeResponse<BruksarealResponse>::egenregistrert).isNotNull().all {
                            prop(BruksarealResponse::metadata).all {
                                prop(RegisterMetadataResponse::registrertAv).isEqualTo("31129956715")
                            }
                        }
                    }
            }
        }

    @Test
    fun `gitt at egenregistrering inneholder kun BRA per etasje og ikke totalt BRA skal man få en bad request i respons`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueIDPortenJWT()

            val response = client.post("/v1/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.ugyldigEgenregistreringMedKunBruksarealPerEtasje(),
                )
                headers {
                    append("Authorization", "Bearer ${token.serialize()}")
                }
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)

            val body = response.body<ErrorResponse.BadRequestError>()

            assertThat(body).all {
                prop(ErrorResponse.BadRequestError::details).isNotNull().size().isEqualTo(1)
            }
        }

    @Test
    fun `gitt manglende idporten token skal man ikke få lov til å kalle endepunktet`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()

            val response = client.post("/v1/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.validEgenregistrering(),
                )
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }

    private fun Assert<List<BruksenhetResponse>>.withBruksenhetId(bruksenhetId: Long) =
        transform(appendName("[bruksenhetId=$bruksenhetId]")) { it.find { br -> br.bruksenhetId == bruksenhetId }!! }
}
