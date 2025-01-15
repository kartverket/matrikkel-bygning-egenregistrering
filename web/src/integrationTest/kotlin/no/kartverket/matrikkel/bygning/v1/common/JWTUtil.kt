package no.kartverket.matrikkel.bygning.v1.common

import no.kartverket.matrikkel.bygning.TestApplicationWithDb.Companion.mockOAuthServer

class JWTUtil {
    companion object {
        private const val DEFAULT_SUBJECT = "123456789"
        private const val DEFAULT_ISSUER = "testIssuer"
        private const val DEFAULT_ORG = "123456789"

        fun getJWTWithScope(scope: String) = mockOAuthServer.issueToken(
            issuerId = DEFAULT_ISSUER,
            subject = DEFAULT_SUBJECT,
            claims = mapOf(
                "orgno" to DEFAULT_ORG,
                "scope" to scope,
            ),
        )

        fun getDefaultMaskinportenJWT() = getJWTWithScope("kartverk:riktig:scope")

        fun getDefaultIDPortenJWT() = mockOAuthServer.issueToken(
            issuerId = DEFAULT_ISSUER,
            subject = DEFAULT_SUBJECT,
            claims = mapOf(
                "orgno" to DEFAULT_ORG,
                "pid" to "31129956715",
            ),
        )
    }
}

