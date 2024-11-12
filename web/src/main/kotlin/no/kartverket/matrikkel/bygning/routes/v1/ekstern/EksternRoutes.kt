package no.kartverket.matrikkel.bygning.routes.v1.ekstern

import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseService
import no.kartverket.matrikkel.bygning.plugins.OpenApiSpecIds
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MASKINPORTEN_PROVIDER_NAME
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MATRIKKEL_AUTH_BERETTIGET_INTERESSE
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MATRIKKEL_AUTH_MED_PERSONDATA
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MATRIKKEL_AUTH_UTEN_PERSONDATA
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.berettigetinteresse.berettigetInteresseRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.hendelse.hendelseRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygningMedPersondataRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.utenPersondataRouting

fun Route.eksternRouting(
    bygningService: BygningService,
    hendelseService: HendelseService,
) {
    route("/") {
        eksternApiRoute("berettigetinteresse", OpenApiSpecIds.BERETTIGET_INTERESSE, MATRIKKEL_AUTH_BERETTIGET_INTERESSE) {
            berettigetInteresseRouting(bygningService)
        }

        eksternApiRoute("utenpersondata", OpenApiSpecIds.UTEN_PERSONDATA, MATRIKKEL_AUTH_UTEN_PERSONDATA) {
            utenPersondataRouting(bygningService)
        }

        eksternApiRoute("medpersondata", OpenApiSpecIds.MED_PERSONDATA, MATRIKKEL_AUTH_MED_PERSONDATA) {
            bygningMedPersondataRouting(bygningService)
        }

        eksternApiRoute("hendelser", OpenApiSpecIds.HENDELSER) {
            hendelseRouting(hendelseService)
        }
    }
}

private fun Route.eksternApiRoute(
    path: String,
    openApiSpecId: String,
    matrikkelAuthSchemeName: String? = null,
    build: Route.() -> Unit
) = route(
    path,
    {
        specName = openApiSpecId
        securitySchemeNames = listOfNotNull(MASKINPORTEN_PROVIDER_NAME, matrikkelAuthSchemeName)
        response {
            code(HttpStatusCode.Unauthorized) {
                description = "Manglende eller ugyldig token"
            }
        }
    },
) {
    if (matrikkelAuthSchemeName != null) {
        authenticate(MASKINPORTEN_PROVIDER_NAME, matrikkelAuthSchemeName, build = build)
    } else {
        authenticate(MASKINPORTEN_PROVIDER_NAME, build = build)
    }
}
