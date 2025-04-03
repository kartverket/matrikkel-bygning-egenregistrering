package no.kartverket.matrikkel.bygning.v1.ekstern.utenpersondata.bygning

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import no.kartverket.matrikkel.bygning.TestApplicationWithDb
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.BruksenhetUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.BygningUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.AvlopKodeUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.BruksarealUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.BruksenhetEtasjerUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.BruksenhetEtasjerUtenPersondataResponse.BruksenhetEtasjeUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.BruksenhetEtasjerUtenPersondataResponse.EtasjeBetegnelseUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.EnergikildeUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.OppvarmingUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.VannforsyningKodeUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.RegisterMetadataUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.BruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EtasjeBetegnelseRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EtasjeBruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueIDPortenJWT
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueMaskinportenJWT
import no.kartverket.matrikkel.bygning.v1.common.gyldigRequest
import org.junit.jupiter.api.Test

class BygningUtenPersondataTest : TestApplicationWithDb() {
    @Test
    fun `gitt en gyldig token med riktig scope skal tilgang gis`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueMaskinportenJWT()

            val response =
                client.get("/v1/utenpersondata/bygninger/1") {
                    bearerAuth(token.serialize())
                }

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.body<BygningUtenPersondataResponse>()).all {
                prop(BygningUtenPersondataResponse::bygningId).isEqualTo(1L)
                prop(BygningUtenPersondataResponse::bruksenheter).hasSize(2)
            }
        }

    @Test
    fun `gitt et token med feil scope skal tilgang nektes`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueMaskinportenJWT("feil:scope")

            val response =
                client.get("/v1/utenpersondata/bygninger/1") {
                    bearerAuth(token.serialize())
                }

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }

    @Test
    fun `gitt et kall uten token skal tilgang nektes`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()

            val response = client.get("/v1/utenpersondata/bygninger/1")

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }

    @Test
    fun `gitt en egenregistrering så skal data uten persondata returneres`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val idportenToken = mockOAuthServer.issueIDPortenJWT()

            val response =
                client.post("/v1/intern/gammel/egenregistreringer") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        EgenregistreringRequest.gyldigRequest().copy(
                            bruksarealRegistrering =
                                BruksarealRegistreringRequest(
                                    totaltBruksareal = 40.0,
                                    etasjeRegistreringer =
                                        listOf(
                                            EtasjeBruksarealRegistreringRequest(
                                                bruksareal = 30.0,
                                                etasjebetegnelse =
                                                    EtasjeBetegnelseRequest(
                                                        etasjeplanKode = "H",
                                                        etasjenummer = 1,
                                                    ),
                                            ),
                                            EtasjeBruksarealRegistreringRequest(
                                                bruksareal = 10.0,
                                                etasjebetegnelse =
                                                    EtasjeBetegnelseRequest(
                                                        etasjeplanKode = "L",
                                                        etasjenummer = 1,
                                                    ),
                                            ),
                                        ),
                                    kildemateriale = KildematerialeKode.Salgsoppgave,
                                ),
                        ),
                    )
                    bearerAuth(idportenToken.serialize())
                }

            assertThat(response.status).isEqualTo(HttpStatusCode.Created)

            val maskinportenToken = mockOAuthServer.issueMaskinportenJWT()
            val bygningResponse =
                client.get("/v1/utenpersondata/bruksenheter/1") {
                    bearerAuth(maskinportenToken.serialize())
                }

            assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
            assertThat(bygningResponse.body<BruksenhetUtenPersondataResponse>()).all {
                prop(BruksenhetUtenPersondataResponse::bruksenhetId).isEqualTo(1L)
                prop(BruksenhetUtenPersondataResponse::etasjer).isNotNull().all {
                    prop(BruksenhetEtasjerUtenPersondataResponse::data).all {
                        index(0).all {
                            prop(BruksenhetEtasjeUtenPersondataResponse::bruksareal).isEqualTo(30.0)
                            prop(BruksenhetEtasjeUtenPersondataResponse::etasjeBetegnelse).all {
                                prop(EtasjeBetegnelseUtenPersondataResponse::etasjeplanKode).isEqualTo("H")
                                prop(EtasjeBetegnelseUtenPersondataResponse::etasjenummer).isEqualTo(1)
                            }
                        }
                        index(1).all {
                            prop(BruksenhetEtasjeUtenPersondataResponse::bruksareal).isEqualTo(10.0)
                            prop(BruksenhetEtasjeUtenPersondataResponse::etasjeBetegnelse).all {
                                prop(EtasjeBetegnelseUtenPersondataResponse::etasjeplanKode).isEqualTo("L")
                                prop(EtasjeBetegnelseUtenPersondataResponse::etasjenummer).isEqualTo(1)
                            }
                        }
                    }
                    prop(BruksenhetEtasjerUtenPersondataResponse::metadata).all {
                        prop(RegisterMetadataUtenPersondataResponse::kildemateriale).isEqualTo(KildematerialeKode.Salgsoppgave)
                    }
                }
                prop(BruksenhetUtenPersondataResponse::totaltBruksareal).isNotNull().all {
                    prop(BruksarealUtenPersondataResponse::metadata).all {
                        prop(RegisterMetadataUtenPersondataResponse::kildemateriale).isEqualTo(KildematerialeKode.Salgsoppgave)
                    }
                }
                prop(BruksenhetUtenPersondataResponse::vannforsyning).isNotNull().all {
                    prop(VannforsyningKodeUtenPersondataResponse::metadata).all {
                        prop(RegisterMetadataUtenPersondataResponse::kildemateriale).isEqualTo(KildematerialeKode.Salgsoppgave)
                    }
                }
                prop(BruksenhetUtenPersondataResponse::avlop).isNotNull().all {
                    prop(AvlopKodeUtenPersondataResponse::metadata).all {
                        prop(RegisterMetadataUtenPersondataResponse::kildemateriale).isEqualTo(KildematerialeKode.Selvrapportert)
                    }
                }
                prop(BruksenhetUtenPersondataResponse::energikilder).isNotNull().all {
                    index(0).isNotNull().all {
                        prop(EnergikildeUtenPersondataResponse::metadata).all {
                            prop(RegisterMetadataUtenPersondataResponse::kildemateriale).isEqualTo(KildematerialeKode.Selvrapportert)
                        }
                    }
                }
                prop(BruksenhetUtenPersondataResponse::oppvarming).isNotNull().all {
                    index(0).isNotNull().all {
                        prop(OppvarmingUtenPersondataResponse::metadata).all {
                            prop(RegisterMetadataUtenPersondataResponse::kildemateriale).isEqualTo(KildematerialeKode.Selvrapportert)
                        }
                    }
                }
            }
        }
}
