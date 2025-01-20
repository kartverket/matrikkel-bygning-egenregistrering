package no.kartverket.matrikkel.bygning.v1.common

import no.nav.security.mock.oauth2.MockOAuth2Server

class JWTUtil() {
    companion object {
        private const val DEFAULT_SUBJECT = "123456789"
        private const val DEFAULT_ISSUER = "testIssuer"
        private const val DEFAULT_ORG = "123456789"

        fun MockOAuth2Server.getJWTWithScope(scope: String) = issueToken(
            issuerId = DEFAULT_ISSUER,
            subject = DEFAULT_SUBJECT,
            claims = mapOf(
                "orgno" to DEFAULT_ORG,
                "scope" to scope,
            ),
        )

        fun MockOAuth2Server.getDefaultMaskinportenJWT() = getJWTWithScope("kartverk:riktig:scope")

        fun MockOAuth2Server.getDefaultIDPortenJWT() = issueToken(
            issuerId = DEFAULT_ISSUER,
            subject = DEFAULT_SUBJECT,
            claims = mapOf(
                "orgno" to DEFAULT_ORG,
                "pid" to "31129956715",
            ),
        )
    }
}

