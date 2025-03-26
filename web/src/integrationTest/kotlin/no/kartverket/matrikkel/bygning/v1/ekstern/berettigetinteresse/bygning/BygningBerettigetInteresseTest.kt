package no.kartverket.matrikkel.bygning.v1.ekstern.berettigetinteresse.bygning

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
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
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.berettigetinteresse.bygning.BruksenhetBerettigetInteresseResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.berettigetinteresse.bygning.BruksenhetEtasjeBerettigetInteresseResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.berettigetinteresse.bygning.BruksenhetEtasjeBerettigetInteresseResponse.EtasjeBetegnelseBerettigetInteresseResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.berettigetinteresse.bygning.BygningBerettigetInteresseResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.BruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EtasjeBetegnelseRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EtasjeBruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueIDPortenJWT
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.issueMaskinportenJWT
import no.kartverket.matrikkel.bygning.v1.common.gyldigRequest
import org.junit.jupiter.api.Test

class BygningBerettigetInteresseTest : TestApplicationWithDb() {
    @Test
    fun `gitt en gyldig token med riktig scope skal tilgang gis`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()
        val token = mockOAuthServer.issueMaskinportenJWT()

        val response = client.get("/v1/berettigetinteresse/bygninger/1") {
            bearerAuth(token.serialize())
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<BygningBerettigetInteresseResponse>()).all {
            prop(BygningBerettigetInteresseResponse::bygningId).isEqualTo(1L)
            prop(BygningBerettigetInteresseResponse::bruksenheter).hasSize(2)
        }
    }

    @Test
    fun `gitt et token med feil scope skal tilgang nektes`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()
        val token = mockOAuthServer.issueMaskinportenJWT("feil:scope")

        val response = client.get("/v1/berettigetinteresse/bygninger/1") {
            bearerAuth(token.serialize())
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `gitt et kall uten token skal tilgang nektes`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()

        val response = client.get("/v1/berettigetinteresse/bygninger/1")

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `gitt en egenregistrering så skal data uten metadata returneres`() = testApplication {
        val client = mainModuleWithDatabaseEnvironmentAndClient()
        val idportenToken = mockOAuthServer.issueIDPortenJWT()

        val response = client.post("/v1/intern/egenregistreringer") {
            contentType(ContentType.Application.Json)
            setBody(
                EgenregistreringRequest.gyldigRequest().copy(
                        bruksarealRegistrering = BruksarealRegistreringRequest(
                                totaltBruksareal = 40.0,
                                etasjeRegistreringer = listOf(
                                        EtasjeBruksarealRegistreringRequest(
                                                bruksareal = 30.0,
                                                etasjebetegnelse = EtasjeBetegnelseRequest(
                                                        etasjeplanKode = "H",
                                                        etasjenummer = 1,
                                                ),
                                        ),
                                        EtasjeBruksarealRegistreringRequest(
                                                bruksareal = 10.0,
                                                etasjebetegnelse = EtasjeBetegnelseRequest(
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
        val bygningResponse = client.get("/v1/berettigetinteresse/bruksenheter/1") {
            bearerAuth(maskinportenToken.serialize())
        }

        assertThat(bygningResponse.status).isEqualTo(HttpStatusCode.OK)
        assertThat(bygningResponse.body<BruksenhetBerettigetInteresseResponse>()).all {
            prop(BruksenhetBerettigetInteresseResponse::bruksenhetId).isEqualTo(1L)
            prop(BruksenhetBerettigetInteresseResponse::etasjer).isNotNull().all {
                index(0).all {
                    prop(BruksenhetEtasjeBerettigetInteresseResponse::bruksareal).isEqualTo(30.0)
                    prop(BruksenhetEtasjeBerettigetInteresseResponse::etasjeBetegnelse).all {
                        prop(EtasjeBetegnelseBerettigetInteresseResponse::etasjeplanKode).isEqualTo("H")
                        prop(EtasjeBetegnelseBerettigetInteresseResponse::etasjenummer).isEqualTo(1)
                    }
                }
                index(1).all {
                    prop(BruksenhetEtasjeBerettigetInteresseResponse::bruksareal).isEqualTo(10.0)
                    prop(BruksenhetEtasjeBerettigetInteresseResponse::etasjeBetegnelse).all {
                        prop(EtasjeBetegnelseBerettigetInteresseResponse::etasjeplanKode).isEqualTo("L")
                        prop(EtasjeBetegnelseBerettigetInteresseResponse::etasjenummer).isEqualTo(1)
                    }
                }
            }
            prop(BruksenhetBerettigetInteresseResponse::totaltBruksareal).isEqualTo(40.0)
            prop(BruksenhetBerettigetInteresseResponse::avlop).isEqualTo(AvlopKode.OffentligKloakk)
            prop(BruksenhetBerettigetInteresseResponse::vannforsyning).isEqualTo(VannforsyningKode.OffentligVannverk)
            prop(BruksenhetBerettigetInteresseResponse::energikilder).isNotNull().all {
                index(0).isEqualTo(EnergikildeKode.Elektrisitet)
            }
            prop(BruksenhetBerettigetInteresseResponse::oppvarming).isNotNull().all {
                index(0).isEqualTo(OppvarmingKode.Elektrisk)
            }
        }
    }
}
