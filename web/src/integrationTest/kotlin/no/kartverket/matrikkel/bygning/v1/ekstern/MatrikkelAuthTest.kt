package no.kartverket.matrikkel.bygning.v1.ekstern

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import no.kartverket.matrikkel.bygning.TestApplicationWithDb
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.MATRIKKEL_AUDIENCE
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.MATRIKKEL_ISSUER
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MatrikkelAuthTest {
    @Nested
    inner class VirksomhetBegrenset : TestApplicationWithDb() {
        @Test
        fun utenToken() =
            testApplication {
                val client = mainModuleWithDatabaseEnvironmentAndClient()

                val response = client.request("/v1/virksomhet_begrenset/bruksenheter/1")
                assertThat(response).prop(HttpResponse::status).isEqualTo(HttpStatusCode.Unauthorized)
            }

        @Test
        fun feilRolle() =
            testApplication {
                val client = mainModuleWithDatabaseEnvironmentAndClient()

                val token =
                    mockOAuthServer.issueToken(
                        issuerId = MATRIKKEL_ISSUER,
                        subject = "stubbed-test-user-noe-annet",
                        audience = MATRIKKEL_AUDIENCE,
                    )

                val response =
                    client.request("/v1/virksomhet_begrenset/bruksenheter/1") {
                        bearerAuth(token.serialize())
                    }
                assertThat(response).prop(HttpResponse::status).isEqualTo(HttpStatusCode.Unauthorized)
            }

        @Test
        fun riktigRolle() =
            testApplication {
                val client = mainModuleWithDatabaseEnvironmentAndClient()

                val token =
                    mockOAuthServer.issueToken(
                        issuerId = MATRIKKEL_ISSUER,
                        subject = "stubbed-test-user-VirksomhetBegrenset",
                        audience = MATRIKKEL_AUDIENCE,
                    )

                val response =
                    client.request("/v1/virksomhet_begrenset/bruksenheter/1") {
                        bearerAuth(token.serialize())
                    }
                assertThat(response).prop(HttpResponse::status).isEqualTo(HttpStatusCode.OK)
            }
    }

    @Nested
    inner class VirksomhetUtvidetUtenPII : TestApplicationWithDb() {
        @Test
        fun utenToken() =
            testApplication {
                val client = mainModuleWithDatabaseEnvironmentAndClient()

                val response = client.request("/v1/virksomhet_utvidet_uten_pii/bruksenheter/1")
                assertThat(response).prop(HttpResponse::status).isEqualTo(HttpStatusCode.Unauthorized)
            }

        @Test
        fun feilRolle() =
            testApplication {
                val client = mainModuleWithDatabaseEnvironmentAndClient()

                val token =
                    mockOAuthServer.issueToken(
                        issuerId = MATRIKKEL_ISSUER,
                        subject = "stubbed-test-user-VirksomhetBegrenset",
                        audience = MATRIKKEL_AUDIENCE,
                    )

                val response =
                    client.request("/v1/virksomhet_utvidet_uten_pii/bruksenheter/1") {
                        bearerAuth(token.serialize())
                    }
                assertThat(response).prop(HttpResponse::status).isEqualTo(HttpStatusCode.Unauthorized)
            }

        @Test
        fun riktigRolle() =
            testApplication {
                val client = mainModuleWithDatabaseEnvironmentAndClient()

                val token =
                    mockOAuthServer.issueToken(
                        issuerId = MATRIKKEL_ISSUER,
                        subject = "stubbed-test-user-VirksomhetUtvidetUtenPII",
                        audience = MATRIKKEL_AUDIENCE,
                    )

                val response =
                    client.request("/v1/virksomhet_utvidet_uten_pii/bruksenheter/1") {
                        bearerAuth(token.serialize())
                    }
                assertThat(response).prop(HttpResponse::status).isEqualTo(HttpStatusCode.OK)
            }
    }

    @Nested
    inner class VirksomhetUtvidet : TestApplicationWithDb() {
        @Test
        fun utenToken() =
            testApplication {
                val client = mainModuleWithDatabaseEnvironmentAndClient()

                val response = client.request("/v1/virksomhet_utvidet/bruksenheter/1")
                assertThat(response).prop(HttpResponse::status).isEqualTo(HttpStatusCode.Unauthorized)
            }

        @Test
        fun feilRolle() =
            testApplication {
                val client = mainModuleWithDatabaseEnvironmentAndClient()

                val token =
                    mockOAuthServer.issueToken(
                        issuerId = MATRIKKEL_ISSUER,
                        subject = "stubbed-test-user-VirksomhetUtvidetUtenPII",
                        audience = MATRIKKEL_AUDIENCE,
                    )

                val response =
                    client.request("/v1/virksomhet_utvidet/bruksenheter/1") {
                        bearerAuth(token.serialize())
                    }
                assertThat(response).prop(HttpResponse::status).isEqualTo(HttpStatusCode.Unauthorized)
            }

        @Test
        fun riktigRolle() =
            testApplication {
                val client = mainModuleWithDatabaseEnvironmentAndClient()

                val token =
                    mockOAuthServer.issueToken(
                        issuerId = MATRIKKEL_ISSUER,
                        subject = "stubbed-test-user-VirksomhetUtvidet",
                        audience = MATRIKKEL_AUDIENCE,
                    )

                val response =
                    client.request("/v1/virksomhet_utvidet/bruksenheter/1") {
                        bearerAuth(token.serialize())
                    }
                assertThat(response).prop(HttpResponse::status).isEqualTo(HttpStatusCode.OK)
            }
    }
}
