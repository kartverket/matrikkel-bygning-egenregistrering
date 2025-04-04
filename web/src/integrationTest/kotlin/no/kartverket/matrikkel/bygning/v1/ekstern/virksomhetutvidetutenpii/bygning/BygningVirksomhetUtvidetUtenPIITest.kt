package no.kartverket.matrikkel.bygning.v1.ekstern.virksomhetutvidetutenpii.bygning

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
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.BruksenhetVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.BygningVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.AvlopKodeVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.BruksarealVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.BruksenhetEtasjerVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.BruksenhetEtasjerVirksomhetUtvidetUtenPIIResponse.BruksenhetEtasjeVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.BruksenhetEtasjerVirksomhetUtvidetUtenPIIResponse.EtasjeBetegnelseVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.EnergikildeVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.OppvarmingVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.VannforsyningKodeVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.RegisterMetadataVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.BruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EtasjeBetegnelseRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EtasjeBruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueIDPortenJWT
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueMaskinportenJWT
import no.kartverket.matrikkel.bygning.v1.common.gyldigRequest
import org.junit.jupiter.api.Test

class BygningVirksomhetUtvidetUtenPIITest : TestApplicationWithDb() {
    @Test
    fun `gitt en gyldig token med riktig scope skal tilgang gis`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueMaskinportenJWT("kartverk:eiendomsregisteret:bygning.virksomhet.utvidet.utenpii")

            val response =
                client.get("/v1/virksomhet_utvidet_uten_pii/bygninger/1") {
                    bearerAuth(token.serialize())
                }

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.body<BygningVirksomhetUtvidetUtenPIIResponse>()).all {
                prop(BygningVirksomhetUtvidetUtenPIIResponse::bygningId).isEqualTo(1L)
                prop(BygningVirksomhetUtvidetUtenPIIResponse::bruksenheter).hasSize(2)
            }
        }

    @Test
    fun `gitt et token med feil scope skal tilgang nektes`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueMaskinportenJWT("feil:scope")

            val response =
                client.get("/v1/virksomhet_utvidet_uten_pii/bygninger/1") {
                    bearerAuth(token.serialize())
                }

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }

    @Test
    fun `gitt et kall uten token skal tilgang nektes`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()

            val response = client.get("/v1/virksomhet_utvidet_uten_pii/bygninger/1")

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }

    @Test
    fun `gitt en egenregistrering så skal data uten persondata returneres`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val idportenToken = mockOAuthServer.issueIDPortenJWT()

            val response =
                client.post("/v1/intern/egenregistreringer") {
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

            val maskinportenToken = mockOAuthServer.issueMaskinportenJWT("kartverk:eiendomsregisteret:bygning.virksomhet.utvidet.utenpii")
            val bygningResponse =
                client.get("/v1/virksomhet_utvidet_uten_pii/bruksenheter/1") {
                    bearerAuth(maskinportenToken.serialize())
                }

            assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
            assertThat(bygningResponse.body<BruksenhetVirksomhetUtvidetUtenPIIResponse>()).all {
                prop(BruksenhetVirksomhetUtvidetUtenPIIResponse::bruksenhetId).isEqualTo(1L)
                prop(BruksenhetVirksomhetUtvidetUtenPIIResponse::etasjer).isNotNull().all {
                    prop(BruksenhetEtasjerVirksomhetUtvidetUtenPIIResponse::data).all {
                        index(0).all {
                            prop(BruksenhetEtasjeVirksomhetUtvidetUtenPIIResponse::bruksareal).isEqualTo(30.0)
                            prop(BruksenhetEtasjeVirksomhetUtvidetUtenPIIResponse::etasjeBetegnelse).all {
                                prop(EtasjeBetegnelseVirksomhetUtvidetUtenPIIResponse::etasjeplanKode).isEqualTo("H")
                                prop(EtasjeBetegnelseVirksomhetUtvidetUtenPIIResponse::etasjenummer).isEqualTo(1)
                            }
                        }
                        index(1).all {
                            prop(BruksenhetEtasjeVirksomhetUtvidetUtenPIIResponse::bruksareal).isEqualTo(10.0)
                            prop(BruksenhetEtasjeVirksomhetUtvidetUtenPIIResponse::etasjeBetegnelse).all {
                                prop(EtasjeBetegnelseVirksomhetUtvidetUtenPIIResponse::etasjeplanKode).isEqualTo("L")
                                prop(EtasjeBetegnelseVirksomhetUtvidetUtenPIIResponse::etasjenummer).isEqualTo(1)
                            }
                        }
                    }
                    prop(BruksenhetEtasjerVirksomhetUtvidetUtenPIIResponse::metadata).all {
                        prop(RegisterMetadataVirksomhetUtvidetUtenPIIResponse::kildemateriale).isEqualTo(KildematerialeKode.Salgsoppgave)
                    }
                }
                prop(BruksenhetVirksomhetUtvidetUtenPIIResponse::totaltBruksareal).isNotNull().all {
                    prop(BruksarealVirksomhetUtvidetUtenPIIResponse::metadata).all {
                        prop(RegisterMetadataVirksomhetUtvidetUtenPIIResponse::kildemateriale).isEqualTo(KildematerialeKode.Salgsoppgave)
                    }
                }
                prop(BruksenhetVirksomhetUtvidetUtenPIIResponse::vannforsyning).isNotNull().all {
                    prop(VannforsyningKodeVirksomhetUtvidetUtenPIIResponse::metadata).all {
                        prop(RegisterMetadataVirksomhetUtvidetUtenPIIResponse::kildemateriale).isEqualTo(KildematerialeKode.Salgsoppgave)
                    }
                }
                prop(BruksenhetVirksomhetUtvidetUtenPIIResponse::avlop).isNotNull().all {
                    prop(AvlopKodeVirksomhetUtvidetUtenPIIResponse::metadata).all {
                        prop(RegisterMetadataVirksomhetUtvidetUtenPIIResponse::kildemateriale).isEqualTo(KildematerialeKode.Selvrapportert)
                    }
                }
                prop(BruksenhetVirksomhetUtvidetUtenPIIResponse::energikilder).isNotNull().all {
                    index(0).isNotNull().all {
                        prop(EnergikildeVirksomhetUtvidetUtenPIIResponse::metadata).all {
                            prop(
                                RegisterMetadataVirksomhetUtvidetUtenPIIResponse::kildemateriale,
                            ).isEqualTo(KildematerialeKode.Selvrapportert)
                        }
                    }
                }
                prop(BruksenhetVirksomhetUtvidetUtenPIIResponse::oppvarming).isNotNull().all {
                    index(0).isNotNull().all {
                        prop(OppvarmingVirksomhetUtvidetUtenPIIResponse::metadata).all {
                            prop(
                                RegisterMetadataVirksomhetUtvidetUtenPIIResponse::kildemateriale,
                            ).isEqualTo(KildematerialeKode.Selvrapportert)
                        }
                    }
                }
            }
        }
}
