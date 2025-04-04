package no.kartverket.matrikkel.bygning.v1.ekstern.virksomhetutvidet.bygning

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
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.BruksenhetVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.BygningVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.AvlopKodeVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.BruksarealVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.BruksenhetEtasjerVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.BruksenhetEtasjerVirksomhetUtvidetResponse.BruksenhetEtasjeVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.BruksenhetEtasjerVirksomhetUtvidetResponse.EtasjeBetegnelseVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.EnergikildeVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.OppvarmingVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.VannforsyningKodeVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.RegisterMetadataVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.BruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EtasjeBetegnelseRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EtasjeBruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueIDPortenJWT
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueMaskinportenJWT
import no.kartverket.matrikkel.bygning.v1.common.gyldigRequest
import org.junit.jupiter.api.Test

class BygningVirksomhetUtvidetTest : TestApplicationWithDb() {
    @Test
    fun `gitt en gyldig token med riktig scope skal tilgang gis`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueMaskinportenJWT("kartverk:eiendomsregisteret:bygning.virksomhet.utvidet")

            val response =
                client.get("/v1/virksomhet_utvidet/bygninger/1") {
                    bearerAuth(token.serialize())
                }

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.body<BygningVirksomhetUtvidetResponse>()).all {
                prop(BygningVirksomhetUtvidetResponse::bygningId).isEqualTo(1L)
                prop(BygningVirksomhetUtvidetResponse::bruksenheter).hasSize(2)
            }
        }

    @Test
    fun `gitt et token med feil scope skal tilgang nektes`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()
            val token = mockOAuthServer.issueMaskinportenJWT("feil:scope")

            val response =
                client.get("/v1/virksomhet_utvidet/bygninger/1") {
                    bearerAuth(token.serialize())
                }

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }

    @Test
    fun `gitt et kall uten token skal tilgang nektes`() =
        testApplication {
            val client = mainModuleWithDatabaseEnvironmentAndClient()

            val response = client.get("/v1/virksomhet_utvidet/bygninger/1")

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

            val maskinportenToken = mockOAuthServer.issueMaskinportenJWT("kartverk:eiendomsregisteret:bygning.virksomhet.utvidet")
            val bygningResponse =
                client.get("/v1/virksomhet_utvidet/bruksenheter/1") {
                    bearerAuth(maskinportenToken.serialize())
                }

            assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
            assertThat(bygningResponse.body<BruksenhetVirksomhetUtvidetResponse>()).all {
                prop(BruksenhetVirksomhetUtvidetResponse::bruksenhetId).isEqualTo(1L)
                prop(BruksenhetVirksomhetUtvidetResponse::etasjer).isNotNull().all {
                    prop(BruksenhetEtasjerVirksomhetUtvidetResponse::data).all {
                        index(0).all {
                            prop(BruksenhetEtasjeVirksomhetUtvidetResponse::bruksareal).isEqualTo(30.0)
                            prop(BruksenhetEtasjeVirksomhetUtvidetResponse::etasjeBetegnelse).all {
                                prop(EtasjeBetegnelseVirksomhetUtvidetResponse::etasjeplanKode).isEqualTo("H")
                                prop(EtasjeBetegnelseVirksomhetUtvidetResponse::etasjenummer).isEqualTo(1)
                            }
                        }
                        index(1).all {
                            prop(BruksenhetEtasjeVirksomhetUtvidetResponse::bruksareal).isEqualTo(10.0)
                            prop(BruksenhetEtasjeVirksomhetUtvidetResponse::etasjeBetegnelse).all {
                                prop(EtasjeBetegnelseVirksomhetUtvidetResponse::etasjeplanKode).isEqualTo("L")
                                prop(EtasjeBetegnelseVirksomhetUtvidetResponse::etasjenummer).isEqualTo(1)
                            }
                        }
                    }
                    prop(BruksenhetEtasjerVirksomhetUtvidetResponse::metadata).all {
                        prop(RegisterMetadataVirksomhetUtvidetResponse::registrertAv).isEqualTo("66860475309")
                    }
                }
                prop(BruksenhetVirksomhetUtvidetResponse::totaltBruksareal).isNotNull().all {
                    prop(BruksarealVirksomhetUtvidetResponse::metadata).all {
                        prop(RegisterMetadataVirksomhetUtvidetResponse::registrertAv).isEqualTo("66860475309")
                    }
                }
                prop(BruksenhetVirksomhetUtvidetResponse::vannforsyning).isNotNull().all {
                    prop(VannforsyningKodeVirksomhetUtvidetResponse::metadata).all {
                        prop(RegisterMetadataVirksomhetUtvidetResponse::registrertAv).isEqualTo("66860475309")
                    }
                }
                prop(BruksenhetVirksomhetUtvidetResponse::avlop).isNotNull().all {
                    prop(AvlopKodeVirksomhetUtvidetResponse::metadata).all {
                        prop(RegisterMetadataVirksomhetUtvidetResponse::registrertAv).isEqualTo("66860475309")
                    }
                }
                prop(BruksenhetVirksomhetUtvidetResponse::energikilder).isNotNull().all {
                    index(0).isNotNull().all {
                        prop(EnergikildeVirksomhetUtvidetResponse::metadata).all {
                            prop(RegisterMetadataVirksomhetUtvidetResponse::registrertAv).isEqualTo("66860475309")
                        }
                    }
                }
                prop(BruksenhetVirksomhetUtvidetResponse::oppvarming).isNotNull().all {
                    index(0).isNotNull().all {
                        prop(OppvarmingVirksomhetUtvidetResponse::metadata).all {
                            prop(RegisterMetadataVirksomhetUtvidetResponse::registrertAv).isEqualTo("66860475309")
                        }
                    }
                }
            }
        }
}
