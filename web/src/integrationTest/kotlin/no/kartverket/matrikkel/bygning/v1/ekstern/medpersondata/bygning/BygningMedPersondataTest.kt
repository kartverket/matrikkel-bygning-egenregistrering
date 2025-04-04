package no.kartverket.matrikkel.bygning.v1.ekstern.medpersondata.bygning

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
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.BruksenhetMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.BygningMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.AvlopKodeMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.BruksarealMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.BruksenhetEtasjerMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.BruksenhetEtasjerMedPersondataResponse.BruksenhetEtasjeMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.BruksenhetEtasjerMedPersondataResponse.EtasjeBetegnelseMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.EnergikildeMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.OppvarmingMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.VannforsyningKodeMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.RegisterMetadataMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.BruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EtasjeBetegnelseRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EtasjeBruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueIDPortenJWT
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueMaskinportenJWT
import no.kartverket.matrikkel.bygning.v1.common.gyldigRequest
import org.junit.jupiter.api.Test

class BygningMedPersondataTest : TestApplicationWithDb() {
    @Test
    fun `gitt en gyldig token med riktig scope skal tilgang gis`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueMaskinportenJWT()

            val response =
                client.get("/v1/medpersondata/bygninger/1") {
                    bearerAuth(token.serialize())
                }

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.body<BygningMedPersondataResponse>()).all {
                prop(BygningMedPersondataResponse::bygningId).isEqualTo(1L)
                prop(BygningMedPersondataResponse::bruksenheter).hasSize(2)
            }
        }

    @Test
    fun `gitt et token med feil scope skal tilgang nektes`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueMaskinportenJWT("feil:scope")

            val response =
                client.get("/v1/medpersondata/bygninger/1") {
                    bearerAuth(token.serialize())
                }

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }

    @Test
    fun `gitt et kall uten token skal tilgang nektes`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()

            val response = client.get("/v1/medpersondata/bygninger/1")

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
                client.get("/v1/medpersondata/bruksenheter/1") {
                    bearerAuth(maskinportenToken.serialize())
                }

            assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
            assertThat(bygningResponse.body<BruksenhetMedPersondataResponse>()).all {
                prop(BruksenhetMedPersondataResponse::bruksenhetId).isEqualTo(1L)
                prop(BruksenhetMedPersondataResponse::etasjer).isNotNull().all {
                    prop(BruksenhetEtasjerMedPersondataResponse::data).all {
                        index(0).all {
                            prop(BruksenhetEtasjeMedPersondataResponse::bruksareal).isEqualTo(30.0)
                            prop(BruksenhetEtasjeMedPersondataResponse::etasjeBetegnelse).all {
                                prop(EtasjeBetegnelseMedPersondataResponse::etasjeplanKode).isEqualTo("H")
                                prop(EtasjeBetegnelseMedPersondataResponse::etasjenummer).isEqualTo(1)
                            }
                        }
                        index(1).all {
                            prop(BruksenhetEtasjeMedPersondataResponse::bruksareal).isEqualTo(10.0)
                            prop(BruksenhetEtasjeMedPersondataResponse::etasjeBetegnelse).all {
                                prop(EtasjeBetegnelseMedPersondataResponse::etasjeplanKode).isEqualTo("L")
                                prop(EtasjeBetegnelseMedPersondataResponse::etasjenummer).isEqualTo(1)
                            }
                        }
                    }
                    prop(BruksenhetEtasjerMedPersondataResponse::metadata).all {
                        prop(RegisterMetadataMedPersondataResponse::registrertAv).isEqualTo("66860475309")
                    }
                }
                prop(BruksenhetMedPersondataResponse::totaltBruksareal).isNotNull().all {
                    prop(BruksarealMedPersondataResponse::metadata).all {
                        prop(RegisterMetadataMedPersondataResponse::registrertAv).isEqualTo("66860475309")
                    }
                }
                prop(BruksenhetMedPersondataResponse::vannforsyning).isNotNull().all {
                    prop(VannforsyningKodeMedPersondataResponse::metadata).all {
                        prop(RegisterMetadataMedPersondataResponse::registrertAv).isEqualTo("66860475309")
                    }
                }
                prop(BruksenhetMedPersondataResponse::avlop).isNotNull().all {
                    prop(AvlopKodeMedPersondataResponse::metadata).all {
                        prop(RegisterMetadataMedPersondataResponse::registrertAv).isEqualTo("66860475309")
                    }
                }
                prop(BruksenhetMedPersondataResponse::energikilder).isNotNull().all {
                    index(0).isNotNull().all {
                        prop(EnergikildeMedPersondataResponse::metadata).all {
                            prop(RegisterMetadataMedPersondataResponse::registrertAv).isEqualTo("66860475309")
                        }
                    }
                }
                prop(BruksenhetMedPersondataResponse::oppvarming).isNotNull().all {
                    index(0).isNotNull().all {
                        prop(OppvarmingMedPersondataResponse::metadata).all {
                            prop(RegisterMetadataMedPersondataResponse::registrertAv).isEqualTo("66860475309")
                        }
                    }
                }
            }
        }
}
