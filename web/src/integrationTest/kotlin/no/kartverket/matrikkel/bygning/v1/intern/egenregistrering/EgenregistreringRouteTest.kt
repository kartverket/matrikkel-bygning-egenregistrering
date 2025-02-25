package no.kartverket.matrikkel.bygning.v1.intern.egenregistrering

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
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
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.AvlopKodeInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BruksarealInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BruksenhetInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.ByggeaarInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.BygningInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.EnergikildeInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.MultikildeInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.OppvarmingInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.RegisterMetadataInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.VannforsyningKodeInternResponse
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

        val response = client.post("/v1/intern/egenregistreringer") {
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

            val response = client.post("/v1/intern/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.validEgenregistrering(),
                )
                headers {
                    append("Authorization", "Bearer ${token.serialize()}")
                }
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.Created)

            val bygningResponse = client.get("/v1/intern/bygninger/1")

            assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
            val bygning = bygningResponse.body<BygningInternResponse>()

            val now = Instant.now()


            assertThat(bygning).all {
                prop(BygningInternResponse::bruksenheter).index(0).all {
                    prop(BruksenhetInternResponse::bruksenhetId).isEqualTo(1L)
                    prop(BruksenhetInternResponse::totaltBruksareal).isNotNull().all {
                        prop(MultikildeInternResponse<BruksarealInternResponse>::egenregistrert).isNotNull().all {
                            prop(BruksarealInternResponse::data).isEqualTo(125.0)
                            prop(BruksarealInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }

                    prop(BruksenhetInternResponse::byggeaar).isNotNull().all {
                        prop(MultikildeInternResponse<ByggeaarInternResponse>::egenregistrert).isNotNull().all {
                            prop(ByggeaarInternResponse::data).isEqualTo(2010)
                            prop(ByggeaarInternResponse::metadata).all {
                                hasRegistreringstidspunktWithinThreshold(now)
                                prop(RegisterMetadataInternResponse::kildemateriale).isEqualTo(KildematerialeKode.Selvrapportert)
                            }
                        }
                    }

                    prop(BruksenhetInternResponse::vannforsyning).isNotNull().all {
                        prop(MultikildeInternResponse<VannforsyningKodeInternResponse>::egenregistrert).isNotNull().all {
                            prop(VannforsyningKodeInternResponse::data).isEqualTo(VannforsyningKode.OffentligVannverk)
                            prop(VannforsyningKodeInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }

                    prop(BruksenhetInternResponse::avlop).isNotNull().all {
                        prop(MultikildeInternResponse<AvlopKodeInternResponse>::egenregistrert).isNotNull().all {
                            prop(AvlopKodeInternResponse::data).isEqualTo(AvlopKode.OffentligKloakk)
                            prop(AvlopKodeInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }

                    prop(BruksenhetInternResponse::energikilder).isNotNull().all {
                        prop(MultikildeInternResponse<EnergikildeInternResponse>::egenregistrert).isNotNull().all {
                            prop(EnergikildeInternResponse::data).containsExactly(EnergikildeKode.Elektrisitet)
                            prop(EnergikildeInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }

                    prop(BruksenhetInternResponse::oppvarminger).isNotNull().all {
                        prop(MultikildeInternResponse<OppvarmingInternResponse>::egenregistrert).isNotNull().all {
                            prop(OppvarmingInternResponse::data).containsExactly(OppvarmingKode.Elektrisk)
                            prop(OppvarmingInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }
                }

                prop(BygningInternResponse::bruksenheter).index(1).all {
                    prop(BruksenhetInternResponse::bruksenhetId).isEqualTo(2L)
                    prop(BruksenhetInternResponse::totaltBruksareal).isNull()
                    prop(BruksenhetInternResponse::energikilder).isNull()
                    prop(BruksenhetInternResponse::oppvarminger).isNull()
                }
            }
        }

    @Test
    fun `gitt en gyldig egenregistrering paa bruksenhet kan bruksenheten hentes ut med de egenregistrerte dataene`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()
        val token = mockOAuthServer.issueIDPortenJWT()

        val response = client.post("/v1/intern/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest.validEgenregistrering(),
            )
            headers {
                append("Authorization", "Bearer ${token.serialize()}")
            }
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)

        val bruksenhetResponse = client.get("/v1/intern/bygninger/1/bruksenheter/1")

        assertThat(bruksenhetResponse.status).isEqualTo(HttpStatusCode.OK)
        val bruksenhet = bruksenhetResponse.body<BruksenhetInternResponse>()

        val now = Instant.now()
        assertThat(bruksenhet).all {
            prop(BruksenhetInternResponse::bruksenhetId).isEqualTo(1L)

            prop(BruksenhetInternResponse::totaltBruksareal).isNotNull().all {
                prop(MultikildeInternResponse<BruksarealInternResponse>::egenregistrert).isNotNull().all {
                    prop(BruksarealInternResponse::data).isEqualTo(125.0)
                    prop(BruksarealInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
            }

            prop(BruksenhetInternResponse::energikilder).isNotNull().all {
                prop(MultikildeInternResponse<EnergikildeInternResponse>::egenregistrert).isNotNull().all {
                    prop(EnergikildeInternResponse::data).containsExactly(EnergikildeKode.Elektrisitet)
                    prop(EnergikildeInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
            }

            prop(BruksenhetInternResponse::oppvarminger).isNotNull().all {
                prop(MultikildeInternResponse<OppvarmingInternResponse>::egenregistrert).isNotNull().all {
                    prop(OppvarmingInternResponse::data).containsExactly(OppvarmingKode.Elektrisk)
                    prop(OppvarmingInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                }
            }
        }
    }

    @Test
    fun `gitt to gyldige egenregistreringer paa bygning og bruksenhet returneres dataene med den nyeste registreringen`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueIDPortenJWT()

            val egenregistrering1 = client.post("/v1/intern/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.validEgenregistrering(),
                )
                headers {
                    append("Authorization", "Bearer ${token.serialize()}")
                }
            }
            assertThat(egenregistrering1.status).isEqualTo(HttpStatusCode.Created)

            val egenregistrering2 = client.post("/v1/intern/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.validEgenregistrering().copy(
                        bruksenhetRegistreringer = listOf(
                            BruksenhetRegistreringRequest(
                                bruksenhetId = 1L,
                                bruksarealRegistrering = BruksarealRegistreringRequest(
                                    totaltBruksareal = 40.0,
                                    etasjeRegistreringer = null,
                                    kildemateriale = KildematerialeKode.Salgsoppgave,
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

            val bygningResponse = client.get("/v1/intern/bygninger/1")

            assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
            val bygning = bygningResponse.body<BygningInternResponse>()

            val now = Instant.now()
            assertThat(bygning).prop(BygningInternResponse::bruksenheter).all {
                withBruksenhetId(1L).all {
                    prop(BruksenhetInternResponse::byggeaar).isNotNull().all {
                        prop(MultikildeInternResponse<ByggeaarInternResponse>::egenregistrert).isNotNull().all {
                            prop(ByggeaarInternResponse::data).isEqualTo(2008)
                            prop(ByggeaarInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }
                    prop(BruksenhetInternResponse::totaltBruksareal).isNotNull().all {
                        prop(MultikildeInternResponse<BruksarealInternResponse>::egenregistrert).isNotNull().all {
                            prop(BruksarealInternResponse::data).isEqualTo(40.0)
                            prop(BruksarealInternResponse::metadata).hasRegistreringstidspunktWithinThreshold(now)
                        }
                    }
                }
                withBruksenhetId(2L).all {
                    prop(BruksenhetInternResponse::byggeaar).isNull()
                    prop(BruksenhetInternResponse::totaltBruksareal).isNull()
                }
            }
        }

    @Test
    fun `gitt at egenregistrering sender info om hvem har registrert blir dette lagret og sendt ut igjen`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueIDPortenJWT()

            val response = client.post("/v1/intern/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                        EgenregistreringRequest.validEgenregistrering(),
                )
                headers {
                    append("Authorization", "Bearer ${token.serialize()}")
                }
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.Created)

            val bygningResponse = client.get("/v1/intern/bygninger/1")

            assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
            val bygning = bygningResponse.body<BygningInternResponse>()

            assertThat(bygning).all {
                prop(BygningInternResponse::bruksareal).isNotNull().prop(MultikildeInternResponse<BruksarealInternResponse>::egenregistrert).isNull()
                prop(BygningInternResponse::bruksenheter).withBruksenhetId(1L)
                    .prop(BruksenhetInternResponse::totaltBruksareal).isNotNull().all {
                        prop(MultikildeInternResponse<BruksarealInternResponse>::egenregistrert).isNotNull().all {
                            prop(BruksarealInternResponse::metadata).all {
                                prop(RegisterMetadataInternResponse::registrertAv).isEqualTo("31129956715")
                            }
                        }
                    }
            }
        }

    @Test
    fun `gitt at egenregistrering inneholder differanse mellom etasje BRA summert og total BRA kal man få en bad request`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueIDPortenJWT()

            val response = client.post("/v1/intern/egenregistreringer") {
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

            val response = client.post("/v1/intern/egenregistreringer") {
                contentType(ContentType.Application.Json)
                setBody(
                    EgenregistreringRequest.validEgenregistrering(),
                )
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }

    private fun Assert<List<BruksenhetInternResponse>>.withBruksenhetId(bruksenhetId: Long) =
        transform(appendName("[bruksenhetId=$bruksenhetId]")) { it.find { br -> br.bruksenhetId == bruksenhetId }!! }
}
