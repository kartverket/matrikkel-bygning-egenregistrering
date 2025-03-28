package no.kartverket.matrikkel.bygning.v1.common

import no.kartverket.matrikkel.bygning.plugins.authentication.ApplicationRoles
import no.nav.security.mock.oauth2.MockOAuth2Server

class MockOAuth2ServerExtensions {
    companion object {
        internal const val DEFAULT_ISSUER = "testIssuer"
        internal const val DEFAULT_AUDIENCE = "default"
        internal const val DEFAULT_PID = "66860475309"

        internal const val MATRIKKEL_ISSUER = "testMatrikkel"
        internal const val MATRIKKEL_AUDIENCE = "matrikkelserver"

        fun MockOAuth2Server.issueMaskinportenJWT(scope: String = "kartverk:riktig:scope") =
            issueToken(
                issuerId = DEFAULT_ISSUER,
                claims =
                    mapOf(
                        "consumer" to
                            mapOf(
                                "authority" to "iso6523-actorid-upis",
                                "ID" to "0192:123456789",
                            ),
                        "scope" to scope,
                    ),
            )

        fun MockOAuth2Server.issueIDPortenJWT() =
            issueToken(
                issuerId = DEFAULT_ISSUER,
                claims =
                    mapOf(
                        "pid" to DEFAULT_PID,
                    ),
            )

        fun MockOAuth2Server.issueM2MEntraJwt(
            audience: String = DEFAULT_AUDIENCE,
            roles: List<String> = listOf(ApplicationRoles.ACCESS_AS_APPLICATION, ApplicationRoles.BYGNING_ARKIVARISK_HISTORIKK),
        ) = issueToken(
            audience = audience,
            issuerId = DEFAULT_ISSUER,
            claims =
                mapOf(
                    "roles" to roles,
                ),
        )
    }
}
