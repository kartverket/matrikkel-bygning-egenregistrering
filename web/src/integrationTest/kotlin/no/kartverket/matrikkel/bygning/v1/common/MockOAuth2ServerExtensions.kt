package no.kartverket.matrikkel.bygning.v1.common

import no.nav.security.mock.oauth2.MockOAuth2Server

class MockOAuth2ServerExtensions {
    companion object {
        private const val DEFAULT_ISSUER = "testIssuer"

        fun MockOAuth2Server.issueMaskinportenJWT(scope: String = "kartverk:riktig:scope") = issueToken(
            issuerId = DEFAULT_ISSUER,
            claims = mapOf(
                "consumer" to mapOf(
                    "authority" to "iso6523-actorid-upis",
                    "ID" to "0192:123456789"
                ),
                "scope" to scope,
            ),
        )

        fun MockOAuth2Server.issueIDPortenJWT() = issueToken(
            issuerId = DEFAULT_ISSUER,
            claims = mapOf(
                "pid" to "31129956715",
            ),
        )
    }
}

