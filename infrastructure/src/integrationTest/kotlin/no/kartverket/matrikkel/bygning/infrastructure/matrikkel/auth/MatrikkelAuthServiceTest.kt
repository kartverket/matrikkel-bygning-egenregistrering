package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingXPath
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.common.ContentTypes
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import kotlinx.coroutines.test.runTest
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.MatrikkelApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.net.URI

@WireMockTest
class MatrikkelAuthServiceTest {
    @Test
    fun invalidUser() =
        runTest {
            wireMock.stubFor(
                WireMock.post("/matrikkelapi/wsapi/v1/BrukerServiceWS").willReturn(WireMock.unauthorized()),
            )

            val matrikkelAuthService = MatrikkelAuthService(MatrikkelApi(URI.create(wireMock.baseUrl())))

            assertThat(matrikkelAuthService.harMatrikkeltilgang("random", Matrikkelrolle.InnsynMedPersondata)).isNull()
        }

    @Test
    fun innsynHeleLandet() =
        runTest {
            wireMock.stubFor(
                WireMock
                    .post("/matrikkelapi/wsapi/v1/BrukerServiceWS")
                    .withHeader(
                        "SOAPAction",
                        equalTo(
                            "\"http://matrikkel.statkart.no/matrikkelapi/wsapi/v1/service/bruker/BrukerService/getPaloggetBrukerRequest\"",
                        ),
                    ).willReturn(
                        okForContentTypeFile(
                            "BrukerService/getPaloggetBrukerResponse.xml",
                        ),
                    ),
            )

            wireMock.stubFor(
                WireMock
                    .post("/matrikkelapi/wsapi/v1/BrukerServiceWS")
                    .withHeader(
                        "SOAPAction",
                        equalTo(
                            "\"http://matrikkel.statkart.no/matrikkelapi/wsapi/v1/service/bruker/BrukerService/findBrukerRettigheterForPaaloggetBrukerRequest\"",
                        ),
                    ).willReturn(
                        okForContentTypeFile(
                            "BrukerService/findBrukerRettigheterForPaaloggetBrukerResponse.xml",
                        ),
                    ),
            )

            wireMock.stubFor(
                WireMock
                    .post("/matrikkelapi/wsapi/v1/StoreServiceWS")
                    .withRequestBody(
                        matchingXPath(
                            "//soapenv:Envelope/soapenv:Body/stor:getObjects/stor:ids[dom:item[dom:value[text()='2']] and dom:item[dom:value[text()='3']]]",
                        ).withXPathNamespace("soapenv", "http://schemas.xmlsoap.org/soap/envelope/")
                            .withXPathNamespace(
                                "stor",
                                "http://matrikkel.statkart.no/matrikkelapi/wsapi/v1/service/store",
                            ).withXPathNamespace("dom", "http://matrikkel.statkart.no/matrikkelapi/wsapi/v1/domain"),
                    ).willReturn(
                        okForContentTypeFile(
                            "StoreService/getObjects_BrukerOgRettighetliste_InnsynMedFnr.xml",
                        ),
                    ),
            )

            val matrikkelAuthService = MatrikkelAuthService(MatrikkelApi(URI.create(wireMock.baseUrl())))

            assertThat(
                matrikkelAuthService.harMatrikkeltilgang(
                    "random",
                    Matrikkelrolle.InnsynMedPersondata,
                ),
            ).isEqualTo("testbruker")

            wireMock.verify(
                2,
                postRequestedFor(
                    urlEqualTo("/matrikkelapi/wsapi/v1/BrukerServiceWS"),
                ).withHeader("Authorization", equalTo("Bearer random")),
            )
            wireMock.verify(
                1,
                postRequestedFor(urlEqualTo("/matrikkelapi/wsapi/v1/StoreServiceWS")).withHeader(
                    "Authorization",
                    equalTo("Bearer random"),
                ),
            )
        }

    @Test
    fun berettigetInteresse_tidligereInnsyn() =
        runTest {
            wireMock.stubFor(
                WireMock
                    .post("/matrikkelapi/wsapi/v1/BrukerServiceWS")
                    .withHeader(
                        "SOAPAction",
                        equalTo(
                            "\"http://matrikkel.statkart.no/matrikkelapi/wsapi/v1/service/bruker/BrukerService/getPaloggetBrukerRequest\"",
                        ),
                    ).willReturn(
                        okForContentTypeFile(
                            "BrukerService/getPaloggetBrukerResponse.xml",
                        ),
                    ),
            )

            wireMock.stubFor(
                WireMock
                    .post("/matrikkelapi/wsapi/v1/BrukerServiceWS")
                    .withHeader(
                        "SOAPAction",
                        equalTo(
                            "\"http://matrikkel.statkart.no/matrikkelapi/wsapi/v1/service/bruker/BrukerService/findBrukerRettigheterForPaaloggetBrukerRequest\"",
                        ),
                    ).willReturn(
                        okForContentTypeFile(
                            "BrukerService/findBrukerRettigheterForPaaloggetBrukerResponse.xml",
                        ),
                    ),
            )

            wireMock.stubFor(
                WireMock
                    .post("/matrikkelapi/wsapi/v1/StoreServiceWS")
                    .withRequestBody(
                        matchingXPath(
                            "//soapenv:Envelope/soapenv:Body/stor:getObjects/stor:ids[dom:item[dom:value[text()='2']] and dom:item[dom:value[text()='3']]]",
                        ).withXPathNamespace("soapenv", "http://schemas.xmlsoap.org/soap/envelope/")
                            .withXPathNamespace(
                                "stor",
                                "http://matrikkel.statkart.no/matrikkelapi/wsapi/v1/service/store",
                            ).withXPathNamespace("dom", "http://matrikkel.statkart.no/matrikkelapi/wsapi/v1/domain"),
                    ).willReturn(
                        okForContentTypeFile(
                            "StoreService/getObjects_BrukerOgRettighetliste_BerettigetInteresse.xml",
                        ),
                    ),
            )

            val matrikkelAuthService = MatrikkelAuthService(MatrikkelApi(URI.create(wireMock.baseUrl())))

            assertThat(matrikkelAuthService.harMatrikkeltilgang("random", Matrikkelrolle.InnsynMedPersondata)).isNull()
            assertThat(
                matrikkelAuthService.harMatrikkeltilgang(
                    "random",
                    Matrikkelrolle.BerettigetInteresse,
                ),
            ).isEqualTo("testbruker")

            wireMock.verify(
                4,
                postRequestedFor(
                    urlEqualTo("/matrikkelapi/wsapi/v1/BrukerServiceWS"),
                ).withHeader("Authorization", equalTo("Bearer random")),
            )
            wireMock.verify(
                2,
                postRequestedFor(urlEqualTo("/matrikkelapi/wsapi/v1/StoreServiceWS")).withHeader(
                    "Authorization",
                    equalTo("Bearer random"),
                ),
            )
        }

    private fun okForContentTypeFile(fileName: String): ResponseDefinitionBuilder =
        WireMock
            .aResponse()
            .withStatus(200)
            .withHeader(ContentTypes.CONTENT_TYPE, *arrayOf("text/xml"))
            .withBodyFile(fileName)

    companion object {
        @Suppress("unused") // Er ikke ubrukt. Er extension.
        @RegisterExtension
        @JvmStatic
        val wireMock: WireMockExtension =
            WireMockExtension
                .newInstance()
                .options(WireMockConfiguration.wireMockConfig().usingFilesUnderClasspath("wiremock").dynamicPort())
                .build()
    }
}
