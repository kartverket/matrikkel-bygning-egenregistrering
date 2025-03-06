package no.kartverket.matrikkel.bygning.routes.v1.ekstern

import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseService
import no.kartverket.matrikkel.bygning.plugins.OpenApiSpecIds
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MASKINPORTEN_PROVIDER_NAME
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.berettigetinteresse.berettigetInteresseRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.hendelse.hendelseRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygningMedPersondataRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.utenPersondataRouting

fun Route.eksternRouting(
    bygningService: BygningService,
    hendelseService: HendelseService,
) {
    authenticate(MASKINPORTEN_PROVIDER_NAME) {
        route(
            "/",
            {
                // Egentlig skal denne ikke være nødvendig siden routen er wrappet i en authenticate block
                // Ser ut til å være en bug i ktor-openapi
                protected = true
            },
        ) {
            routeWithMaskinporten("berettigetinteresse", OpenApiSpecIds.BERETTIGET_INTERESSE) {
                berettigetInteresseRouting(bygningService)
            }

            routeWithMaskinporten("utenpersondata", OpenApiSpecIds.UTEN_PERSONDATA) {
                utenPersondataRouting(bygningService)
            }

            routeWithMaskinporten("medpersondata", OpenApiSpecIds.MED_PERSONDATA) {
                bygningMedPersondataRouting(bygningService)
            }

            routeWithMaskinporten("hendelser", OpenApiSpecIds.HENDELSER) {
                hendelseRouting(hendelseService)
            }
        }
    }
}

private fun Route.routeWithMaskinporten(path: String, openApiSpecId: String, build: Route.() -> Unit) = route(
    path,
    {
        specName = openApiSpecId
        securitySchemeNames = listOf(MASKINPORTEN_PROVIDER_NAME)
        response {
            code(HttpStatusCode.Unauthorized) {
                description = "Manglende eller ugyldig token"
            }
        }
    },
    build,
)
