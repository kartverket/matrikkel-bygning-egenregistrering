package no.kartverket.matrikkel.bygning.v1.ekstern

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import no.kartverket.matrikkel.bygning.TestApplicationWithFakes
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.error.AuthorizationError
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetId
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.MATRIKKEL_AUDIENCE
import no.kartverket.matrikkel.bygning.v1.common.MockOAuth2ServerExtensions.Companion.MATRIKKEL_ISSUER
import java.util.*
import kotlin.test.Test

class AuthorizationErrorTest : TestApplicationWithFakes() {
    @Test
    fun forbidden() = testApplication {
        val client = mainModuleWithClient {
            bygningService = mockk {
                coEvery { getBruksenhetByBubbleId(1L, any()) } returns Err(AuthorizationError)
            }
        }

        val token = mockOAuthServer.issueToken(
            issuerId = MATRIKKEL_ISSUER,
            subject = "stubbed-test-user-BerettigetInteresse",
            audience = MATRIKKEL_AUDIENCE,
        )

        val response = client.request("/v1/berettigetinteresse/bruksenheter/1") {
            bearerAuth(token.serialize())
        }
        assertThat(response).prop(HttpResponse::status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun allowed() = testApplication {
        val client = mainModuleWithClient {
            bygningService = mockk {
                coEvery { getBruksenhetByBubbleId(1L, any()) } returns Ok(
                    Bruksenhet(
                        BruksenhetId(UUID.randomUUID()),
                        BruksenhetBubbleId(1),
                    ),
                )
            }
        }

        val token = mockOAuthServer.issueToken(
            issuerId = MATRIKKEL_ISSUER,
            subject = "stubbed-test-user-BerettigetInteresse",
            audience = MATRIKKEL_AUDIENCE,
        )

        val response = client.request("/v1/berettigetinteresse/bruksenheter/1") {
            bearerAuth(token.serialize())
        }
        assertThat(response).prop(HttpResponse::status).isEqualTo(HttpStatusCode.OK)
    }
}
