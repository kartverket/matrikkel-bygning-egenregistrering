package no.kartverket.matrikkel.bygning.routes.v1.ekstern

import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseService
import no.kartverket.matrikkel.bygning.plugins.OpenApiSpecIds
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.MASKINPORTEN_PROVIDER_NAME
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.hendelse.hendelseRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygningMedPersondataRouting

fun Route.eksternRouting(
    bygningService: BygningService,
    hendelseService: HendelseService,
) {
    authenticate(MASKINPORTEN_PROVIDER_NAME) {
        route("/") {
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
        specId = openApiSpecId
        securitySchemeNames = listOf(MASKINPORTEN_PROVIDER_NAME)
        response {
            code(HttpStatusCode.Unauthorized) {
                description = "Manglende eller ugyldig token"
            }
        }
    },
    build
)
